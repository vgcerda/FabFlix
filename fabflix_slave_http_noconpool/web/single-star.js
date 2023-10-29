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
    let singleStarNameElement =jQuery("#movie-star-name");
    let singleMovieTableBodyElement = jQuery("#single_star_table_body");

    movielisthref.attr("href", "index.html"+ resultData["prevURL"]);

    // Concatenate the html tags with resultData jsonObject
    let starName = data[0]["single_star_name"];
    singleStarNameElement.append(starName);

    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + data[0]["single_star_name"] + "</th>";
    rowHTML += "<th>" + data[0]["single_star_year"] + "</th>";
    rowHTML += "<th>";
    let movies = data[0]["single_star_movies"];
    for (let j = 0; j < movies.length; j++) {
        let x = movies[j].split("/");
        let movie_name = x[0];
        let movie_id = x[1];
        rowHTML += '<a href = "single-movie.html?id=' + movie_id + '">';
        rowHTML += movie_name;
        if (j !== movies.length - 1) {
            rowHTML += ", ";
        }
    }
    rowHTML += "</th>"
    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    singleMovieTableBodyElement.append(rowHTML);
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSingleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});