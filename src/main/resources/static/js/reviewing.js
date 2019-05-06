$(document).ready(function() {

    const startDate = $("#startDate").text();
    const endDate = $("#endDate").text();
    const tagSelection = $("#tagSelection").text();

    console.log(tagSelection);

    getReviewList(startDate, endDate, tagSelection);

});

function getReviewList(startDate, endDate, tagSelection) {
    const postParameters = {
        "startDate" : startDate,
        "endDate" : endDate,
        "tagSelection" : tagSelection
    };

    $.post("getReviewList", postParameters, responseJSON => {

        const responseObject = JSON.parse(responseJSON);

        

        console.log("review response");
        console.log(responseObject);

    });
}