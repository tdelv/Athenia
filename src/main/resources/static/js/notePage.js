
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

function insertText() {
    console.log("inserting text");
}

function insertVocab() {
    console.log("inserting vocab");
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