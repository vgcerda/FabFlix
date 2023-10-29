/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleSingleMovieResult(resultData) {
    console.log("handleSingleMovieResult: populating single-movie table from resultData");

    let data = resultData["data"];

    let movielisthref = jQuery("#movie-list-href");
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let singleMovieNameElement = jQuery("#single_movie_title");
    let singleMovieTableBodyElement = jQuery("#single_movie_table_body");
    if (resultData["prevURL"] === null) {
        movielisthref.attr("href", "search.html");
    } else {
        movielisthref.attr("href", "index.html" + resultData["prevURL"]);
    }

    singleMovieNameElement.append(data[0]["single_movie_title"]);

    // Concatenate the html tags with resultData jsonObject
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + data[0]["single_movie_title"] + "</th>";
    rowHTML += "<th>" + data[0]["single_movie_year"] + "</th>";
    rowHTML += "<th>" + data[0]["single_movie_director"] + "</th>";

    if (data[0]["single_movie_genres"] === "N/A") {
        rowHTML += "<th>" + "N/A" + "</th>";
    }
    else {
        rowHTML += "<th>";

        let genres = data[0]["single_movie_genres"];
        let genre = "";
        for (let j = 0; j < genres.length; j++) {
            genre = genres[j]
            rowHTML += "<a href='index.html?type=browse&by=genre&genre=" +
                genre + "'>" + genre + '</a>';
            if (j !== genres.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";
    }

    if (data[0]["single_movie_stars"] === "N/A") {
        rowHTML += "<th>" + "N/A" + "</th>";
    }
    else {
        rowHTML += "<th>";

        let stars = data[0]["single_movie_stars"];

        for (let j = 0; j < stars.length; j++) {
            let x = stars[j].split("/");
            let star_name = x[0];
            let star_id = x[1];
            rowHTML += '<a href = "single-star.html?id=' + star_id + '">'
            rowHTML += star_name;
            if (j !== stars.length - 1) {
                rowHTML += ", ";
            }
        }

        rowHTML += "</th>";
    }

    rowHTML += "<th>" + data[0]["single_movie_rating"] + "</th>";

    rowHTML += "<th><form onsubmit='handleCartInfo(jQuery(this).serialize()+ \"&action=plus\")' id='cart' METHOD='POST'> " +
        "<input type='hidden' id='itemId' name='itemId' value='" + data[0]['singe_movie_id'] + "'>" +
        "<input type='hidden' id='title' name='title' value='" + data[0]['single_movie_title'] + "'>" +
        "<input type='SUBMIT' value='Add To Cart' name='action'>" +
        "</form></th>";

    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    singleMovieTableBodyElement.append(rowHTML);
}

function handleCartInfo(cart) {
    console.log("single-movie.js: submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    console.log(cart);
    $.ajax("api/cart", {
        method: "POST",
        data: cart,
        success: resultDataString => {
            alert("success: Added to Cart");
        }
    });
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSingleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});