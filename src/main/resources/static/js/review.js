
let $startDate;
let $endDate;
let $tagSelection;

$(document).ready(function() {

    $startDate = $("#startDate");
    $endDate = $("#endDate");
    $tagSelection = $("#tagSelection");
    const $submitButton = $("#submitButton");

    $submitButton.on("click", function(e) {
       e.preventDefault();
       submitReviewForm();
    });

});

function submitReviewForm() {
    const startDateVal = $startDate.val();
    const endDateVal = $endDate.val();
    const tagSelectionVal = $tagSelection.val();

    const location = "reviewingMode?startDate=" + startDateVal
                        + "&endDate=" + endDateVal
                        + "&tagSelection=" + tagSelectionVal;

    window.location = location;
}