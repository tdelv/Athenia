
$( document ).ready(function() {

    $("#addLanguageButton").click(addNewLanguage);

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
            document.location.reload(true);
        });

    }

}