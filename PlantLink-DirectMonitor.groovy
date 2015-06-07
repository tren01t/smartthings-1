/**
 *  PlantLink-Direct Monitor
 *
 *  Copyright 2015 Sidney Johnson
 *	Inspired by notoriousbdg's BatteryMonitor 
 *
 *  If you like this app, please support the developer via PayPal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Version: 1.0 - Initial Version
 *	Version: 1.01 - Added humidity to the display
 */
definition(
    name: "PlantLink-Direct Monitor",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Monitors plantlink sencors",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home10-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home10-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home10-icn@3x.png")


preferences {
    page name:"pageInfo"
    page name:"pageConf"
}

def pageInfo() {
	return dynamicPage(name: "pageInfo", title: "PlantLink-Direct Monitor", install: true, uninstall: true) {
		section("About") {
		paragraph "PlantLink-Direct Monitor, Monitors your Plantlinks via Kristopher Kubicki's plantlink-direct devicetype, and sends notifacations when your plants need water."
		paragraph "${textVersion()}\n${textCopyright()}"
		}
        def plantlist = ""
		settings.senors.each() {
            try {
				plantlist += "$it.displayName is $it.currentStatus at $it.currentHumidity%\n"
            } catch (e) {
                log.trace "Error checking status."
                log.trace e
            }
        }
		if (plantlist) {
			section("Your Plants...") {
				paragraph plantlist.trim()
			}
		}
		section() {
			href "pageConf", title: "Configure"
		}
	}
}

def pageConf() {
	return dynamicPage(name: "pageConf", title: "PlantLink-Direct Monitor", nextPage: "pageInfo") {
		section("Select Sensors...") {
			input "senors", "device.PlantlinkDirectSensor", title: "PlantLink-Direct...", multiple: true, required: true
		}
		section("Select events to to recieve notifications for..."){
        	input "sendPush", "enum", title: "Push?", options: ["Yes", "No"], required: false, defaultValue: "Yes"
            input "sendSMS", "phone", title: "SMS?", required: false
            input "sendTooDry", "enum", title: "Too Dry?", options: ["Yes", "No"], required: false, defaultValue: "Yes"
			input "sendTooWet", "enum", title: "Too Wet?", options: ["Yes", "No"], required: false, defaultValue: "No"
            input "sendTime", "time", title: "At what time?", required: true
            
		}
	}
}

def installed() {
	log.trace "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	log.info "PlantLink-Direct Monitor ${textVersion()} ${textCopyright()}"
	schedule(settings.sendTime, sendStatus)
}

def sendStatus() {
	log.trace "Checking Plants"
    sendEvent(linkText:app.label, name:"sendStatus", descriptionText:"Checking Plants", eventType:"SOLUTION_EVENT", displayed: true)
    settings.senors.each() {
        try {
        	log.trace "${it.displayName} is ${it.currentStatus}"
			sendEvent(linkText:app.label, name:"${it.displayName}", value:"${it.currentStatus} at ${it.currentHumidity}%", descriptionText:"${it.displayName} is ${it.currentStatus} at ${it.settings.humidity}", eventType:"SOLUTION_EVENT", displayed: true)
            if (it.currentStatus == "Too Wet" && settings.sendTooWet == "Yes") {
                send("${it.displayName} is ${it.currentStatus}")
            }
			if (it.currentStatus == "Too Dry" && settings.sendTooDry == "Yes") {
                send("${it.displayName} is ${it.currentStatus}")
            }
        } catch (e) {
            log.trace "Error checking status"
            log.trace e
        }
    }
}

def send(msg) {
    log.trace msg

    if (settings.sendPush) {
        sendPush(msg)
    } else {
        sendNotificationEvent(msg)
    }

    if (settings.sendSMS != null) {
        sendSms(phoneNumber, msg) 
    }
}

private def textVersion() {
    def text = "Version 1.01"
}

private def textCopyright() {
    def text = "Copyright © 2015 Sidjohn1"
}