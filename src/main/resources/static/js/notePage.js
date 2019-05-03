
// INSERT VOCAB BUTTON JQUERY ELEMENT
const ivb = $("#insertVocabButton");


$( document ).ready(function() {

    const tt = new Tooltip($("#insertTextButton"), {
        placement: "right",
        title: "Top",
        trigger: "hover"
    });

    // ivb.hover(function(){
    //     console.log("yeet");
    //     tt.show()});

    $("#insertTextButton").click(insertText);
    $("#insertVocabButton").click(insertVocab);
    $("#insertConjugationButton").click(insertConjugation);
    $("#insertExclamationButton").click(insertExclamation);
    $("#insertQuestionButton").click(insertQuestion);
});

function renderText(content) {
    $("#noteBody").append(`<p>${content}</p>`);
}

function insertText() {
    console.log("inserting text");

    renderText("write here!");
}

function insertVocab() {
    console.log("inserting vocab");
    // vocabularyAdd
    // VocabularyAddHandler

    const postParameters = {id: "hackergirlxoxo", newTerm: "term", newDef: "definition"};

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