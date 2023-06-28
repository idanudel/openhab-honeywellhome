# HoneywellHome Binding

_This binding is used to connect your HoneywellHome (only T5/T6 thermostat for now) using official [HoneywellHome Api](https://developer.honeywellhome.com/api-methods)._ 

_The binding supports discovery via configuring your HoneywellHome dev App (describe down below)._ 

_From the binding, you will get status of your thermostat and also a command channel where you can control the thermostat._ 

_Since the binding uses a polling mechanism, there may be some latency depending on your setting regarding refresh time_

## Supported Things
Coming Soon

## Discovery
Coming Soon

## Binding Configuration
1. Creating Honeywell developer account
2. Create openhab Honeywell app in your Honeywell developer account
3. Bind your openhab Honeywell app to your Honeywell thermostats account
4. Get Access Token
5. Create openhab Thing

### 1. Creating Honeywell developer account:
Create Honeywell developer account via https://developer.honeywellhome.com/
### 2. Create openhab Honeywell app in your Honeywell developer account:
![step_2](.github/images/step_2.png?raw=true)

* callback url is not really matter for this beta version

### 3. Bind your Openhab Honeywell app to your Honeywell thermostats account:

Open your browser with this URL

```https://api.honeywell.com/oauth2/authorize?response_type=code&redirect_uri={your Honeywell openhab app Callback URL }&client_id={your Honeywell openhab app client_id}```

* your “Honeywell openhab app Callback URL” & your “Honeywell openhab app client_id” can be found in your developer account under “My APPS” (client_id = Consumer Key)


![step_3](.github/images/step_3.png?raw=true)

After hitting the URL above with your URL parameters you will start your Auth 2 process, meaning Honeywell will ask to give permissions to your new Openhab app you just created, so you will need to log in to your Honeywell client account (same user password you login with your mobile app to control your thermostat) and allow your Honeywell openhab app permissions.

![step_3](.github/images/step_3_2.png?raw=true)
![step_3](.github/images/step_3_3.png?raw=true)

Next, you will be forwarded to your callback URL, in the URL you will notice "code" query parameters, copy this value - we will need it for step number 4.
in the following example, our code is "EtggGS9x": 
https://myopenhab.org/static/honeywellhome-oauth2.html?code=EtggGS9x&scope=

### 4. Get Access Token:
In order to get the Access Token, you will need to do a POST request to ```https://api.honeywell.com/oauth2/token```
with your code from step number 3 and your `Consumer Key`, `Consumer Secret`, and `call back url` from your openhab App 

you will need to create a basic auth token with your `Consumer Key` and `Consumer Secret, you can use [this]([https://developer.honeywellhome.com/api-methods](https://mixedanalytics.com/tools/basic-authentication-generator/)) for that (`Consumer Key`:`Consumer Secret`),
and add to the following parameters as ``` application/x-www-form-urlencoded``` 
grant_type = authorization_code
code = {the code you got from step number 3}
redirect_uri = {your app redirect_uri} (don't know y they need it)

so it will look like curl:

```
curl --location 'https://api.honeywell.com/oauth2/token' \
--header 'Authorization: Basic {YOUR_BASIC_AUTH TOKEN}' \
--header 'Accept: application/json' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=authorization_code' \
--data-urlencode 'code={YOUR_CODE_FROM_STEP_3}' \
--data-urlencode 'redirect_uri={YOUR_REDIRECT_URL}'
```

as a response, you finely get your `access_token` and `refresh_token`. keep them - you will need them in step number 5.

### 5. Create Openhab Thing:




## Thing Configuration
Coming Soon

### `sample` Thing Configuration
Coming Soon

## Channels
Coming Soon

## Full Example
Coming Soon
### Thing Configuration

```java
Coming Soon
```
### Item Configuration

```java
Coming Soon
```

### Sitemap Configuration

```perl
Coming Soon
```

## Any custom content here!

Coming Soon
