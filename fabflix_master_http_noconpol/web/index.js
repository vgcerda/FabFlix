// let search_param = $("#search-params");
// let cart = $("#cart");
/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function appendToCurrentURL(param){
    form.action = window.location.href;
    let params = new URLSearchParams(window.location.search);
    params.forEach(function(value, key) {
        if (key === "sortBy"){
            return;
        }
        let x = document.createElement("input");
        x.type = "hidden";
        x.name = key;
        x.value = value;
        form.appendChild(x);
    });
    return true;
}

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

function addCart(){

}

function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    // let movieListHref = jQuery("#movie_list_href");
    // let url = "";
    // let text = "";
    //
    //
    // let typeOfFilter = getParameterByName("type");
    // if (typeOfFilter === "search"){
    //     url = "search.jsp";
    //     text = "Search";
    // }
    // else if (typeOfFilter === "browse"){
    //     url = "browse.jsp";
    //     text = "Browse";
    // }
    // else {
    //
    // }


    // movieListHref.attr("href", url);
    // movieListHref.text(text);

    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length - 1; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">' +
            resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        if (resultData[i]["movie_genres"] === "N/A") {
            rowHTML += "<th>" + "N/A" + "</th>";
        }
        else {
            rowHTML += "<th>";
            let genre = "";
            for (let j = 0; j < resultData[i]["movie_genres"].length; j++) {
                genre = resultData[i]["movie_genres"][j]
                rowHTML += "<a href='index.html?type=browse&by=genre&genre=" +
                    genre + "'>" + genre + '</a>';
                if (j !== resultData[i]["movie_genres"].length - 1) {
                    rowHTML += ", ";
                }
            }
            rowHTML += "</th>";
        }

        if (resultData[i]["movie_stars"] === "N/A") {
            rowHTML += "<th>" + "N/A" + "</th>";
        }
        else {
            rowHTML += "<th>";
            for (let j = 0; j < resultData[i]["movie_stars"].length; j++) {
                let x = resultData[i]["movie_stars"][j].split("/");
                let star_name = x[0];
                let star_id = x[1];
                rowHTML += '<a href = "single-star.html?id=' + star_id + '">';
                rowHTML += star_name;
                if (j !== resultData[i]["movie_stars"].length - 1) {
                    rowHTML += ", ";
                }
            }
            rowHTML += "</th>";
        }

        rowHTML += "<th>" + resultData[i]["movie_ratings"] + "</th>";
        rowHTML += "<th><form onsubmit='handleCartInfo(jQuery(this).serialize()+ \"&action=plus\")' id='cart' METHOD='POST'> " +
            "<input type='hidden' id='itemId' name='itemId' value='" + resultData[i]['movie_id'] + "'>" +
            "<input type='hidden' id='title' name='title' value='" + resultData[i]['movie_title'] + "'>" +
            "<input type='SUBMIT' value='Add To Cart' name='action'>" +
            "</form></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }

    let keepNextButton = resultData[resultData.length - 1]["keep"];
    console.log(keepNextButton);

    let rowHTML = "";
    let pNum = getParameterByName('page');
    let moviePreviousButtonElement = jQuery("#previous-button");
    if(pNum !== null && pNum !== '1'){
        rowHTML += '<input TYPE="Submit" VALUE="Prev">';
        moviePreviousButtonElement.append(rowHTML);
    }

    rowHTML = "";
    let movieNextButtonElement = jQuery("#next-button");
    if(keepNextButton){
        rowHTML += '<input TYPE="Submit" VALUE="Next">';
        movieNextButtonElement.append(rowHTML);
    }

}

// function handleSearch(searchParams) {
//     console.log("im here")
//     console.log(searchParams);
//
//     // let title = $("#search-params input[name=title]").val()
//     // let director = $("#search-params input[name=director]").val()
//     // let year = $("#search-params input[name=year]").val()
//     // let star_name = $("#search-params input[name=star_name]").val()
//     // let type = $("#search-params input[name=type]").val()
//     let title = document.getElementsByTagName("title")
//     let director = document.getElementsByTagName("director")
//     let year = document.getElementsByTagName("year")
//     let star_name = document.getElementsByTagName("star_name")
//     let type = document.getElementsByTagName("type")
//
//     searchParams.preventDefault();
//
//     $.ajax({
//         dataType: "json",
//         method: "GET",
//         url: "api/movies?title=" + title
//             + "&director=" + director
//             + "&year=" + year
//             + "&star_name=" + star_name
//             + "&type=" + type,
//         success: resultData => handleMovieResult(resultData)
//     });
// }

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let params =  new URLSearchParams(window.location.search);
let type = params.get("type");
let sortBy = params.get("sortBy");
let page = params.get("page");
let n = params.get("nElements");

console.log("sortby: ", sortBy);
console.log("page: ", page);


if (sortBy == null || sortBy === ""){
    sortBy = "none";
}

if(n == null || n === ""){
    n = "10";
}

let p = "";
if(page == null || page === ""){
    p = 1;
}else{
    p = page;
}

if (type === "search") {
    let title = params.get("title");
    let director = params.get("director");
    let year = params.get("year");
    let star_name = params.get("star_name");

    $.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies?title=" + title
            + "&director=" + director
            + "&year=" + year
            + "&star_name=" + star_name
            + "&type=" + type
            + "&sortBy=" + sortBy
            + "&nElements=" + n
            + "&page=" + p,
        success: resultData => handleMovieResult(resultData)
    });
}

if (type === "browse") {
    let by = params.get("by");

    let browseParams = "";

    if (by === "title") {
        let title_start = params.get("titlestart");
        browseParams += "&by=title&titlestart=" + title_start;
    }
    if (by === "genre") {
        let genre = params.get("genre");
        browseParams += "&by=genre&genre=" + genre;
    }

    $.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies?type=" + type
            + browseParams
            + "&sortBy=" + sortBy
            + "&nElements=" + n
            + "&page=" + p,
        success: resultData => handleMovieResult(resultData)
    });
}

// function handleAddCartResp(data){
//     $('#msg').html(data).fadeIn('slow');
//     //$('#msg').html("data insert successfully").fadeIn('slow') //also show a success message
//     $('#msg').delay(5000).fadeOut('slow');
// }

// let cart = $("#cart");

function handleCartInfo(cart) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    // cartEvent.preventDefault();
    console.log(cart);
    // console.log(cartEvent["target"]);
    // console.log(cartEvent["target"].getAttributeNames());
    // console.log(cartEvent["target"].children);
    // let params = {
    //     "title": cartEvent["target"].getAttribute("title"),
    //     "itemId": cartEvent["target"].getAttribute("itemId")
    // }
    // console.log(cart["title"]);
    // console.log(cart.attr("title"))
    // console.log(cart.getAttribute("title"));

    $.ajax("api/cart", {
        method: "POST",
        data: cart,
        success: resultDataString => {
            alert("success: Added to Cart");
            // let resultDataJson = JSON.parse(resultDataString);
            // handleAddCartResp("Added to Cart");
        }
    });

    // clear input form
    // cart.reset();
}

// cart.submit(handleCartInfo);

// Makes the HTTP GET request and registers on success callback function handleStarResult
// jQuery.ajax({
//     dataType: "json", // Setting return data type
//     method: "GET", // Setting request method
//     url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
//     success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
// });
//     ?title=" + getParameterByName("title")
// + "&director=" + getParameterByName("director")
// + "&year=" + getParameterByName("year")
// + "&star_name=" + getParameterByName("star_name")
// + "&type=" + getParameterByName("type")

// search_param.submit(handleSearch);