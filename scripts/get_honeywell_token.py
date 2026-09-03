#!/usr/bin/env python3
"""
Automates the Honeywell OAuth2 "get access + refresh token" step from the binding's
README (steps 3 and 4), so you don't have to hand-build a browser URL, copy a `code`
query param out of an address bar, and hand-craft a curl command with a Basic Auth
header.

Usage:
    python3 scripts/get_honeywell_token.py \
        --client-id YOUR_CONSUMER_KEY --client-secret YOUR_CONSUMER_SECRET

Before running:
  1. In your Honeywell developer app, set the callback URL to exactly:
         http://localhost:8087/callback
     (or pass --redirect-uri to use a different host/port - it must match what's
     registered on the Honeywell app.)

What it does:
  1. Opens your browser to Honeywell's authorize URL.
  2. Runs a local HTTP server on localhost to catch the OAuth redirect and grab the
     `code` query parameter automatically - no copying it out of the address bar.
  3. Exchanges that code for an access_token/refresh_token via the token endpoint.
  4. Prints the values ready to paste into the Honeywell Home Account Bridge Thing.

No third-party dependencies - stdlib only, Python 3.
"""
import argparse
import base64
import http.server
import json
import sys
import urllib.error
import urllib.parse
import urllib.request
import webbrowser

# Resideo migrated the API off api.honeywell.com (whose cert was left to expire, breaking every
# integration - see github.com/home-assistant/core/issues/171362) to api.honeywellhome.com.
AUTHORIZE_URL = "https://api.honeywellhome.com/oauth2/authorize"
TOKEN_URL = "https://api.honeywellhome.com/oauth2/token"


def parse_args():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--client-id", required=True, help="Consumer Key from your Honeywell developer app")
    parser.add_argument("--client-secret", required=True, help="Consumer Secret from your Honeywell developer app")
    parser.add_argument("--redirect-uri", default="http://localhost:8087/callback",
                         help="Must exactly match the callback URL configured on your Honeywell app "
                              "(default: %(default)s)")
    parser.add_argument("--no-browser", action="store_true",
                         help="Don't auto-open a browser, just print the authorize URL")
    return parser.parse_args()


def wait_for_code(redirect_uri):
    parsed = urllib.parse.urlparse(redirect_uri)
    if parsed.hostname not in ("localhost", "127.0.0.1"):
        print("--redirect-uri host is not localhost/127.0.0.1 - this script can only catch the "
              "redirect automatically for a localhost callback.")
        return input("Paste the 'code' value from the URL you landed on: ").strip()

    port = parsed.port or 80
    captured = {}

    class Handler(http.server.BaseHTTPRequestHandler):
        def do_GET(self):
            query = urllib.parse.urlparse(self.path).query
            params = urllib.parse.parse_qs(query)
            code = params.get("code", [None])[0]
            captured["code"] = code
            self.send_response(200)
            self.send_header("Content-Type", "text/html")
            self.end_headers()
            body = ("<html><body><h3>Got it - you can close this tab.</h3></body></html>" if code else
                    "<html><body><h3>No 'code' parameter found. Check the terminal for details.</h3></body></html>")
            self.wfile.write(body.encode("utf-8"))

        def log_message(self, fmt, *args):
            pass  # silence default request logging, keep the script's own output clean

    server = http.server.HTTPServer(("localhost", port), Handler)
    print(f"Waiting for the OAuth redirect on {redirect_uri} ...")
    server.handle_request()  # blocks for exactly one request, then returns
    return captured.get("code")


def exchange_code_for_tokens(client_id, client_secret, code, redirect_uri):
    basic_auth = base64.b64encode(f"{client_id}:{client_secret}".encode("utf-8")).decode("ascii")
    data = urllib.parse.urlencode({
        "grant_type": "authorization_code",
        "code": code,
        "redirect_uri": redirect_uri,
    }).encode("utf-8")
    req = urllib.request.Request(TOKEN_URL, data=data, method="POST", headers={
        "Authorization": f"Basic {basic_auth}",
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded",
    })
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        print(f"Token exchange failed: HTTP {e.code}\n{body}", file=sys.stderr)
        sys.exit(1)


def main():
    args = parse_args()
    authorize_url = AUTHORIZE_URL + "?" + urllib.parse.urlencode({
        "response_type": "code",
        "redirect_uri": args.redirect_uri,
        "client_id": args.client_id,
    })

    print(f"Authorize URL:\n  {authorize_url}\n")
    if not args.no_browser:
        webbrowser.open(authorize_url)
    else:
        print("Open that URL in a browser and log in to Honeywell to authorize this app.")

    code = wait_for_code(args.redirect_uri)
    if not code:
        print("Did not receive an authorization 'code'. Aborting.", file=sys.stderr)
        sys.exit(1)

    print("Got authorization code, exchanging it for tokens...")
    tokens = exchange_code_for_tokens(args.client_id, args.client_secret, code, args.redirect_uri)

    print("\nSuccess! Paste these into the Honeywell Home Account Bridge Thing:\n")
    print(f"  Consumer Key:    {args.client_id}")
    print(f"  Consumer Secret: {args.client_secret}")
    print(f"  Token:           {tokens.get('access_token')}")
    print(f"  Refresh Token:   {tokens.get('refresh_token')}")
    print(f"\n(access token expires in {tokens.get('expires_in')}s - the binding refreshes it "
          f"automatically after that.)")


if __name__ == "__main__":
    main()
