/*
 
  Exchange Rate Utility
  
  Author: Warren Gavin
 
  This utility was written to explore some basic parts of groovy. Its
  not intended to be an actual utility used on a regular basis.
 
 */

// TODO: Better understand the appropriate usage of assert. 
// TODO: Reduce request down to currencies.
// TODO: Find out if using main is best practice.
// TODO: Build out the CliBuilder with more options e.g. convert etc.
// TODO: Add command line option to list all currency codes.
// TODO: Add command line option to search currency codes or descriptions.
// TODO: Create class.
// TODO: Add command line option for rounding .round(#).
// TODO: Find a way to lookup currency symbol.
// TODO: Get code formatting to work in sublilme with BUSL.
// TODO: Possibly provide an interactive mode (for fun).
// TODO: Ass base currency configuration.


import groovy.json.JsonSlurper
import groovy.json.JsonOutput


class OpenExchangeRates {

    // Building URL for latest currency values.
    private latestUrlString

    // Building URL for available currencies.
    private currencyCodeURL

    OpenExchangeRates() {

        def json = new File("ExchangeRate.json")

        def config = new JsonSlurper().parseText(json.text)

        assert config
        assert config.api_url
        assert config.api_key

        latestUrlString = "${config.api_url}latest.json?app_id=${config.api_key}"
        currencyCodeURL = "${config.api_url}currencies.json?app_id=${config.api_key}"
        
    }

    def latest(codes) {

        def httpConnection = latestUrlString.toURL().openConnection()

        assert httpConnection.responseCode == httpConnection.HTTP_OK

        def result = new JsonSlurper().parse(httpConnection.inputStream.newReader())

    }

    def currencyCodes() {

        def httpConnection = currencyCodeURL.toURL().openConnection()

        assert httpConnection.responseCode == httpConnection.HTTP_OK

        def result = new JsonSlurper().parse(httpConnection.inputStream.newReader())

    }
}






// Building basic CliBuilder.
def cli = new CliBuilder( usage:'groovy ExchangeRate [currency]' )


// Validating that arguments have been passed, if not exit.
if ( ! args ) {

    cli.usage()

    System.exit( 0 )

}


// Retrieving options passed via the command line and asserting they exist.
def options = cli.parse( args )

assert options && options.arguments()

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


def oer = new OpenExchangeRates()

def currencyCodes = oer.currencyCodes()

assert currencyCodes

// Validating that the currencies passed as arguments exists.
arguments.each { 

    // If the currency is not found, display message to the user and exit.
    if ( ! currencyCodes[it] ) {
        
        println ""
        println "The currency code provided cannot be found."
        println ""
        
        cli.usage()

        System.exit( 0 )
    }

}

def latest = oer.latest(arguments)


// Print each currency, rate and description.
arguments.each {

    def descr = currencyCodes[ it ]
    def rate = latest.rates[ it ]
    def inverse = 1 / rate

    println "$it:$rate:$inverse:$descr"

}

System.exit(0)