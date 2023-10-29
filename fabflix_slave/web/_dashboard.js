let add_star_params = $("#add-star-params");
let add_movie_params = $("#add-movie-params");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */

function handleAddStar(resultData){
    alert("Success: Added a new star\n" + "Star Id: " + resultData["starId"]);
}

function handleAddMovie(resultData){
    alert("Success: Added a new star\n"
        + "Movie Id: " + resultData["movieId"] + "\n"
        + "Star Id: " + resultData["starId"] + "\n"
        + "Genre Id: " + resultData["genreId"]);
}

function handleMetaDataResult(resultData){
    console.log(resultData);
    let metaTableElement = jQuery("#metadata_table");
    for(let i = 0; i < resultData.length - 1; i++){
        let tableHTML = "";
        tableHTML += "<table class='table table-striped'>";
        tableHTML += "<caption>" + resultData[i]["table_name"] + "</caption>";
        tableHTML += "<thread>";
        tableHTML += "<tr>";
        tableHTML += "<th>Attribute Name</th>";
        tableHTML += "<th>Attribute Type</th>";
        tableHTML += "</tr>";
        tableHTML += "</thread>";
        tableHTML += "<tbody>";
        for(let j = 0; j < resultData[i]["column_array"].length - 1; j++){
            let colObj = resultData[i]["column_array"][j];
            tableHTML += "<tr>";
            tableHTML += "<th>"+ colObj["column_name"] +"</th>";
            tableHTML += "<th>"+ colObj["datatype"] +"</th>";
            tableHTML += "</tr>";
        }
        tableHTML += "</tbody>";
        metaTableElement.append(tableHTML);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitStarForm(formSubmitEvent) {
    console.log("submit new Star form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "_dashboard/action", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: add_star_params.serialize(),
            success: (resultData) => handleAddStar(resultData),
            error: function(){
                alert("Error: Unable to add new star");
            }
        }
    );
    add_star_params[0].reset();
}

function submitMovieForm(formSubmitEvent) {
    console.log("submit new Movie form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "_dashboard/action", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: add_movie_params.serialize(),
            success: (resultData) => handleAddMovie(resultData),
            error: function(){
                alert("Error: Unable to add new movie");
            }
        }
    );
    add_movie_params[0].reset();
}

$.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "_dashboard/metadata", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMetaDataResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

// Bind the submit action of the form to a handler function
add_star_params.submit(submitStarForm);
add_movie_params.submit(submitMovieForm);
