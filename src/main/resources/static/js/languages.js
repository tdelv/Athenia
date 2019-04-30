
const SELECTED_CLASS_NAME = "selected";
const SELECTED_SELECTOR = "." + SELECTED_CLASS_NAME + " span";

$( document ).ready(function() {

    $("#addLanguageButton").click(addNewLanguage);
    $("#removeLanguageButton").click(removeLanguage);
    $(".languageSelectForRemoveButton").click(function() {editSelection(this)});
    $(".languageCard").click(function() {languageSelect(this)});

});

function addNewLanguage() {

    const newLanguage = $("#languageInput").val();

    if (!newLanguage) {
        console.log("user must enter a nonempty string");
    } else {
        console.log("new lang: " + newLanguage);

        const postParameters = {newLanguage: newLanguage};

        // DONE: Make a POST request to the "/validate" endpoint with the word information
        $.post("/addNewLanguage", postParameters, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            if (responseObject.successful) {
                document.location.reload(true);
            } else {
                console.log("message: " + message);
            }
        });

    }

}

function removeLanguage() {

    const lang2beRemoved = $(SELECTED_SELECTOR).html();

    if (lang2beRemoved) {
        const postParameters = {language: lang2beRemoved};
        // TODO: set up this request in backend
        $.post("/removeLanguage", postParameters, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            if (responseObject.successful) {
                document.location.reload(true);
            } else {
                console.log("message: " + message);
            }
        });
    }
}

function editSelection(lang) {
    $(".languageSelectForRemoveButton").removeClass(SELECTED_CLASS_NAME);
    $(lang).addClass(SELECTED_CLASS_NAME);
}

function languageSelect(lang) {

    const childElt = lang.children[0];
    const language = $(childElt).html();

    // TODO: tell the backend that this (language) is the current language

    window.location = "home"
}