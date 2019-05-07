
// These are used for the delete functionality
const SELECTED_CLASS_NAME = "selected";
const SELECTED_SELECTOR = "." + SELECTED_CLASS_NAME + " span";

$( document ).ready(function() {
    $("#addLanguageButton").click(addNewLanguage);
    $("#removeLanguageButton").click(removeLanguage);
    $(".languageSelectForRemoveButton").click(function() {editSelection(this)});
    $(".languageCard").hover(langHover, langLeave);
    $(".languageCard").click(function() {languageSelect(this)});
});

function langHover() {
    document.body.style.cursor = "pointer";
}

function langLeave() {
    document.body.style.cursor = "default";
}


function addNewLanguage() {

    const newLanguage = $("#languageInput").val();

    if (!newLanguage) {
        // TODO: modal dialoge displaying this message
        console.log("user must enter a nonempty string");
    } else {
        console.log("new lang: " + newLanguage);
        const postParameters = {newLanguage: newLanguage};

        $.post("/addNewLanguage", postParameters, responseJSON => {
            const responseObject = JSON.parse(responseJSON);
            if (responseObject.successful) {
                document.location.reload(true);
            } else {
                console.log("message: " + responseObject.message);
            }
        });
    }

}

function removeLanguage() {

    const lang2beRemoved = $(SELECTED_SELECTOR).html();

    if (lang2beRemoved) {
        const postParameters = {language: lang2beRemoved};
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

    const postParameters = {language: language};

    $.post("/changeCurrentLanguage", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            window.location = "home";
        } else {
            console.log("message: " + message);
        }
    });

}