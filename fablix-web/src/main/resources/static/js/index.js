'use strict';
let lookupCache = new Map();

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    query = query.split(/\s+/).filter(w=>w.length > 0).sort().join(" ");
    if(lookupCache.has(query)) {
        console.log("hit in cache: " + query);
        doneCallback(lookupCache.get(query));
        return;
    }
    console.log("sending AJAX request to backend Java Servlet")
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "/movie/suggestion?title=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    console.log(data);
    let jsonData = JSON.parse(data);
    console.log(jsonData)

    lookupCache.set(query, {suggestions: jsonData});

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}

function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"])
    window.location.href = "/movie/singlemovie?id="+escape(suggestion["data"]["movieId"]);
}
$(document).ready(function () {
    $('#search-form input').keypress(function(e) {
        if(e.which == 13) {
            e.preventDefault();
            $('#search-form').submit();
        }
    })

    $('#title').autocomplete({
        lookupLimit: 10,
        deferRequestBy: 300,
        minChars: 3,
        lookup: function (query, doneCallback) {
            handleLookup(query, doneCallback)
        },
        onSelect: function(suggestion) {
            handleSelectSuggestion(suggestion)
        },
    });
})