
$( document ).ready(function() {

    let tt = new Tooltip($("#insertTextButton"), {
        placement: "right",
        title: "Top",
        trigger: "hover"
    });

    $("#insertTextButton").click(insertText);
    $("#insertVocabButton").click(insertVocab);
    $("#insertConjugationButton").click(insertConjugation);
    $("#insertExclamationButton").click(insertExclamation);
    $("#insertQuestionButton").click(insertQuestion);
});

function insertText() {
    console.log("inserting text");
    // TODO: post request which generates the text in the backend
    // pull info from post request to generate the text object in the front end
    const newText = new TextModule("id123weee", "date created", "date modified", "sample content");
    $("#noteBody").append(newText.toHTML());
    newText.setUp();

}

function insertVocab() {
    console.log("inserting vocab");
    // vocabularyAdd
    // VocabularyAddHandler

    const postParameters = {newTerm: "term", newDef: "definition"};

    $.post("/vocabularyAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newGirl = responseObject.newVocabModule;
            console.log("here she is: " + newGirl);
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}

function insertConjugation() {
    console.log("inserting conjugation");
}

function insertExclamation() {
    console.log("inserting exclamation");
}

function insertQuestion() {
    console.log("inserting question");
}