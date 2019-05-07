let index = 0;
let modules;
let $moduleContainer;

$(document).ready(function() {

    // pull important information for reviewing
    const startDate = $("#startDate").text();
    const endDate = $("#endDate").text();
    const tagSelection = $("#tagSelection").text();

    $moduleContainer = $("#moduleContainer");

    const $rightButton = $("#rightButton");
    const $leftButton = $("#leftButton");

    // event handlers for left button
    $leftButton.on("click", function() {
       index--;
       displayModule();
    });

    // event handlers for right button
    $rightButton.on("click", function() {
       index++;
       displayModule();
    });

    // run the post request to generate review data
    getReviewList(startDate, endDate, tagSelection);
});

/**
 * Pulls the list of things to review from the backend.
 *
 * @param startDate - the startDate to review from
 * @param endDate - the endDate to review to
 * @param tagSelection - the selection of tags which are being reviewed
 */
function getReviewList(startDate, endDate, tagSelection) {
    const postParameters = {
        "startDate" : startDate,
        "endDate" : endDate,
        "tagSelection" : tagSelection
    };

    // post request for pulling the data
    $.post("getReviewList", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);

        // check for success
        if (responseObject.successful) {
            modules = responseObject.reviewModules;
            displayModule();

            // handle an unsuccessful run
        } else {
            console.log(responseObject.message);
        }
    });
}

/**
 * Displays the specific module at any given index so long as the index
 * module list is populated.
 */
function displayModule() {
    // check if modules list is not null
    if (modules !== null) {

        // check for index out of bounds
        if (index >= 0 && index < modules.length) {
            const moduleToShow = modules[index];

            // empty the page container and populate with the new module
            $moduleContainer.empty();
            const moduleToShowType = createModule(moduleToShow);
            $moduleContainer.append(moduleToShowType.toHTML());

            // handle index out of bounds
        } else {
            if (index < 0) {
                index = modules.length - 1;
                displayModule();
            } else if (index >= modules.length) {
                index = 0;
                displayModule();
            }
        }

        // catch if modules list is empty
    } else {
        index = 0;
        console.log("modules is null");
    }
}

/**
 * Determines which module type to return if the modtype of the generic
 * module is of a reviewable type.
 *
 * @param module - the module to create a specific modtype of
 * @returns {*} - a reviewable instantiation of the module if it is recognized
 */
function createModule(module) {

    const modtype = module.modtype;

    if (modtype === "VOCAB") {
        return new Vocabulary(module);

    } else if (modtype === "NOTE") {
        return new Note(module);

    } else if (modtype === "CONJUGATION") {
        console.log("TODO IF WE WANT TO IMPLEMENT CONJUGATIONS");
        return null;

    } else {
        console.log("reviewable module type is not recognized");
        return null;
    }
}