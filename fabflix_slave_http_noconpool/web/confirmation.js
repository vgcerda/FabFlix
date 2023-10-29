
function handleConfirmationData(resultDataString) {
    let salesCart = resultDataString;
    console.log("handle Confirmation response");
    console.log(salesCart);

    let confirmationTableBodyElement = jQuery("#confirmation_table_body");

    let totalPrice = 0;

    for (const key in salesCart){
        if(salesCart.hasOwnProperty(key)){
            let qty = salesCart[key]["quantity"];
            console.log("saleId: " + key);
            console.log("qty: " + qty);
            let endRange = parseInt(key) + qty - 1;
            let rowHTML = "";
            rowHTML += "<tr>";
            rowHTML += "<th>" + key + "-" + endRange.toString() + "</th>";
            rowHTML += "<th>" + salesCart[key]["title"] + "</th>";
            rowHTML += "<th>" + salesCart[key]["quantity"] + "</th>";
            rowHTML += "<th>" + salesCart[key]["price"] + "</th>";
            rowHTML += "<th>";
            totalPrice += qty * salesCart[key]["price"];
            confirmationTableBodyElement.append(rowHTML);
        }
    }

    let finalPrice = jQuery("#final_price");
    finalPrice.text("Total Cost: $"+ String(totalPrice));

}

$.ajax("api/payments", {
    dataType: "json",
    method: "GET",
    success: (resultData) => handleConfirmationData(resultData)
});