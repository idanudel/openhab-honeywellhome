# HoneywellHome Binding

_This binding is used to connect your HoneywellHome (only T5/T6 thermostat for now) using official [HoneywellHome Api](https://developer.honeywellhome.com/api-methods)._ 

_The binding supports discovery via configuring your HoneywellHome dev App (describe down below)._ 

_From the binding, you will get status of your thermostat and also a command channel where you can control the thermostat (very basic in this point)._ 

_Since the binding uses a polling mechanism, there may be some latency depending on your setting regarding refresh time (15 sec is the minimum, less than that will cause 429 rate limiter errors from HoneywellHome servers)_

***
Binding Jar available [Here](https://github.com/idanudel/openhab-honeywellhome/releases)
***
## Honeywell Home Api Rate limiter
Seems like HoneywellHome Api allow 3000 requests per hour per "openhab app" - more than that will start get a 429 response,
that means if you have two thermostats you will need to change your refresh interval to 30 sec.
(if you will create different "openhab app" per thermostat you can put it back to 15 sec).
* This part is still in progress 
* The behavior I see it not align with the [HoneywellHome Rate Limiter Document](https://developer.honeywellhome.com/faqs/what-rate-limit-api)

## Supported Things
Coming Soon...

## Discovery
Coming Soon...

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

Next, you will be forwarded to your callback URL, in the URL you will notice "code" query parameter, copy this value - we will need it for step number 4.
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
After installing the addon you will create `Honeywell Home Account Binding Thing` and insert your
1. Consumer Key - from step number 3
2. Consumer Secret - from step number 3
3. Token - from step number 4
4. Refresh Token - from step number 4

and Hit create Thing
![step_5](.github/images/step_5.png?raw=true)

Then go back to `HoneywellHome Binding` again and hit scan
![step_5_2](.github/images/step_5_2.png?raw=true)

then you should see your thermostats.

Clicking on one of them will create honeywellhome thermostat thing
![step_5_3](.github/images/step_5_3.png?raw=true)

## Channels
Coming Soon

## Full Example
Items:
```
Number CoolSetPoint "Cool SetPoint" {channel="honeywellhome:thermostat:9bf4a5d4fa:LCC-B82CA02CE73E:coolSetPoint"}
Number HeatSetpoint "Heat Set Point" {channel="honeywellhome:thermostat:9bf4a5d4fa:LCC-B82CA02CE73E:heatSetpoint"}
String ThermostatSetpointStatus "Thermostat Set Point Status" {channel="honeywellhome:thermostat:9bf4a5d4fa:LCC-B82CA02CE73E:thermostatSetpointStatus"}
String HeatCoolMode "Heat Cool Mode" {channel="honeywellhome:thermostat:9bf4a5d4fa:LCC-B82CA02CE73E:heatCoolMode"}
String Mode "Mode" {channel="honeywellhome:thermostat:9bf4a5d4fa:LCC-B82CA02CE73E:mode"}
String FanStatus "FanStatus" {channel="honeywellhome:thermostat:9bf4a5d4fa:LCC-B82CA02CE73E:fanStatus"}
```
Sitemap:
```
Frame label="Thermostat"{
    Setpoint item=HeatSetpoint icon="temperature" label="heatSetpoint [%.1f °F]" minValue=82 maxValue=100 step=1
    Setpoint item=CoolSetPoint icon="temperature" label="coolSetPoint [%.1f °F]" minValue=68 maxValue=82 step=1
    Text item=ThermostatSetpointStatus icon="temperature" label="heatSetpoint"
    Text item=HeatCoolMode icon="temperature" label="heatCoolMode"
    Selection item=Mode label="Mode" mappings=[Off="Off", Cool="Cool", Heat="Heat"]
    Selection item=FanStatus label="Fan Status" mappings=[On="On", Auto="Auto", Circulate="Circulate"]
}

```
