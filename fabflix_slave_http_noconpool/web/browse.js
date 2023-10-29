/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let browseTableBodyElement = jQuery("#browse_table_body");

    // Iterate through resultData, no more than 10 entries
    let rowHTML = "";

    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject


        if (i % 5 === 0) {
            rowHTML += "<tr>";
        }
        let genre = resultData[i]["genre"];
        rowHTML += "<th><form action='index.html' method='GET'>" +
            "<a href='index.html?type=browse&by=genre&genre=" + genre + "'>" + genre +
            "</a></form></th>";
        if ((i + 1) % 5 === 0) {
            rowHTML += "</tr>";
            browseTableBodyElement.append(rowHTML);
            rowHTML = "";
        }
        // Append the row created to the table body, which will refresh the page
    }

    if (rowHTML !== "") {
        rowHTML += "</tr>";
        browseTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/browse",
    success: resultData => handleResult(resultData)
});