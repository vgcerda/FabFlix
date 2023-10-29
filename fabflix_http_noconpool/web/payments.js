let payment = $("#payment-form");

/**
 * Handle the data returned by PaymentServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handlePaymentData(resultDataString) {
    let shoppingCart = resultDataString["shoppingCart"];

    console.log("handle Payment response");
    console.log(shoppingCart);

    let paymentsTableBodyElement = jQuery("#payments_table_body");

    let totalPrice = 0;

    for (const key in shoppingCart){
        if(shoppingCart.hasOwnProperty(key)){
            let rowHTML = "";
            rowHTML += "<tr>";
            rowHTML += "<th>" + shoppingCart[key]["title"] + "</th>";
            rowHTML += "<th>" + shoppingCart[key]["quantity"] + "</th>";
            rowHTML += "<th>" + shoppingCart[key]["price"] + "</th>";
            rowHTML += "<th>";
            totalPrice += shoppingCart[key]["quantity"] * shoppingCart[key]["price"];
            paymentsTableBodyElement.append(rowHTML);
        }
    }

    let finalPrice = jQuery("#final_price");
    finalPrice.text("Total Cost: $"+ String(totalPrice));

}

/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handlePaymentInfo(cartEvent) {
    console.log("submitted payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.ajax("api/payments", {
        method: "POST",
        data: payment.serialize(),
        success: () => {
            window.location.replace("confirmation.html");
        },
        error: function(){
            alert("Incorrect Card Information");
        }
    });

    // clear input form
    payment[0].reset();
}

$.ajax("api/cart", {
    dataType: "json",
    method: "GET",
    success: (resultData) => handlePaymentData(resultData)
});

// Bind the submit action of the form to a event handler function
payment.submit(handlePaymentInfo);