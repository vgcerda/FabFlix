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
function handleCheckoutResult(resultData) {
    console.log("handleCheckoutResult: populating checkout table from resultData");
    let data = resultData;
    let shoppingCart = data["shoppingCart"];
    let checkoutTableBodyElement = jQuery("#checkout_table_body");
    console.log(shoppingCart);

    for (const key in shoppingCart){
        if(shoppingCart.hasOwnProperty(key)){
            let rowHTML = "";
            rowHTML += "<tr>";
            rowHTML += "<th>" + shoppingCart[key]["title"] + "</th>";
            rowHTML += "<th>" + shoppingCart[key]["quantity"] + "</th>";
            rowHTML += "<th>" + shoppingCart[key]["price"] + "</th>";
            rowHTML += "<th><form onsubmit='handleCheckoutInfo(jQuery(this).serialize() + \"&action=minus\")' id='checkout-cart' METHOD='POST'> " +
                "<input type='hidden' id='itemId' name='itemId' value='" + key + "'>" +
                "<input type='hidden' id='title' name='title' value='" + shoppingCart[key]['title'] + "'>" +
                "<input type='SUBMIT' id='minus' value='-'>" +
                "</form></th>";
            rowHTML += "<th><form onsubmit='handleCheckoutInfo(jQuery(this).serialize() + \"&action=plus\")' id='checkout-cart' METHOD='POST'> " +
                "<input type='hidden' id='itemId' name='itemId' value='" + key + "'>" +
                "<input type='hidden' id='title' name='title' value='" + shoppingCart[key]['title'] + "'>" +
                "<input type='SUBMIT' id='plus' value='+'>" +
                "</form></th>";
            rowHTML += "<th><form onsubmit='handleCheckoutInfo(jQuery(this).serialize() + \"&action=remove\")' id='checkout-cart' METHOD='POST'> " +
                "<input type='hidden' id='itemId' name='itemId' value='" + key + "'>" +
                "<input type='hidden' id='title' name='title' value='" + shoppingCart[key]['title'] + "'>" +
                "<input type='SUBMIT' id='plus' value='Remove From Cart'>" +
                "</form></th>";
            rowHTML += "</tr>";
            checkoutTableBodyElement.append(rowHTML);
        }
    }
}

function handleCheckoutInfo(cart){

    console.log(cart);
    $.ajax("api/cart", {
        method: "POST",
        data: cart,
        success: resultDataString => {
            // let resultDataJson = JSON.parse(resultDataString);
            // handleAddCartResp("Added to Cart");
        }
    });
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/cart", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleCheckoutResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});