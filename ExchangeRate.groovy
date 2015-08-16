/*
 
  Exchange Rate Utility
  
  Author: Warren Gavin
 
  This utility was written to explore some basic parts of groovy. Its
  not intended to be an actual utility used on a regular basis.

  The Open Exchange Rates service provides both free and pay subscriptions. For
  this script I only setup the free subscription. This script could be updated
  to support additional features e.g. changing the base currency, requesting
  specific currencies.

  Future enhancements for paid service:

  - Pull rates for specific currencies.
  - Set the base currency.
 
 */

// TODO: Better understand the appropriate usage of assert. 
// TODO: Find out if using main is best practice.
// TODO: Build out the CliBuilder with more options e.g. convert etc.
// TODO: Add command line option to list all currency codes.
// TODO: Add command line option to search currency codes or descriptions.
// TODO: Find a way to lookup currency symbol.
// TODO: Look up currency codes in file, if exist, or download.
// TODO: Catch errors from API (e.g. setting base)

package com.dubelyoo

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class OpenExchangeRates {

    // Building URL for latest currency values.
    private latestUrlString

    // Building URL for available currencies.
    private currencyCodeURL

    // JsonSlurper
    private js

    OpenExchangeRates() {

        js = new JsonSlurper()

        def config = js.parseText new File("ExchangeRate.json").text

        assert config
        assert config.api_url
        assert config.api_key
        assert config.base_currency

        def parameters = "app_id=${config.api_key}&base=${config.base_currency}"

        latestUrlString = "${config.api_url}latest.json?${parameters}"
        currencyCodeURL = "${config.api_url}currencies.json?${parameters}"
        
    }

    def latest(codes) {

        def httpConnection = latestUrlString.toURL().openConnection()

        assert httpConnection.responseCode == httpConnection.HTTP_OK

        def result = js.parse httpConnection.inputStream.newReader()

    }

    def currencyCodes() {

        def httpConnection = currencyCodeURL.toURL().openConnection()

        assert httpConnection.responseCode == httpConnection.HTTP_OK

        def result = js.parse httpConnection.inputStream.newReader()

    }

}

// Building basic CliBuilder.
def cli = new CliBuilder( usage:'groovy ExchangeRate [options] <currency/search>' )

cli.h longOpt:'help', 'Displays usage'
cli.s longOpt:'search', 'Search for a currency code'
cli._ longOpt:'disclaimer', 'Display disclaimer'
cli._ longOpt:'license', 'Display license agreement'
cli._ longOpt:'currencies', 'Displays all available currencies'


// Validating that arguments have been passed, if not exit.
if ( ! args ) {

    cli.usage()

    System.exit(0)

}


// Retrieving options passed via the command line and asserting they exist.
def options = cli.parse args

assert options

if (options.h) {
    cli.usage()
    System.exit(0)
}



def oer = new OpenExchangeRates()

def currencyCodes = oer.currencyCodes()

assert currencyCodes


// Display all available currency codes and descriptions.
// Will exit after all are printed to STDOUT.
if (options.currencies) {

    currencyCodes.each { code, descr ->
        println "$code,$descr"
    }

    System.exit(0)
}


// At this point there must be currency codes passed.
assert options.arguments()

// Retrieving arguments and asserting a few things.
def arguments = options.arguments()

assert arguments && arguments.size() > 0


// Validating that each currency argument passed is in the correct form.
arguments.each {

    if ( ! ( it ==~ ( /[A-Z]{3}/ ) ) ) {

        println ""
        println "Currency codes can only contain 3 characters [a-zA-Z]."
        println "$it is not in propper form, e.g. USD"
        println ""

        cli.usage()

        System.exit(0)

    }
}

// Validating that the currencies passed as arguments exists.
arguments.each { 

    // If the currency is not found, display message to the user and exit.
    if ( ! currencyCodes[it] ) {
        
        println ""
        println "The currency $it code provided cannot be found."
        println ""
        
        cli.usage()

        System.exit( 0 )
    }

}

def latest = oer.latest arguments

if (options.disclaimer) { println "\nDISCLAIMER\n\n$latest.disclaimer\n" }
if (options.license) { println "\nLICENSE\n\n$latest.license\n" }


println "CODE,RATE,INVERSE,TIMESTAMP,DESCRIPTION"

// Print each currency, rate and description.
arguments.each {

    def descr = currencyCodes[it]
    def rate = latest.rates[it]
    def inverse = 1 / rate

    println "$it,$rate,$inverse,$latest.timestamp,$descr"

}

System.exit(0)