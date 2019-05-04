
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

function insertModule(module) {
    $("#noteBody").append(module.toHTML());
    module.setUp();
}

function insertNote() {
    console.log("inserting text");
    const postParameters = {newTerm: "content"};
    $.post("noteAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newNote = responseObject.newNoteModule;
            const newNoteModule = new Vocabulary(newNote.id, newNote.dateCreated, newNote.dateModified, newNote.noteContent);
            insertModule(newNoteModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}

function insertVocab() {
    console.log("inserting vocab");
    const postParameters = {newTerm: "term", newDef: "definition"};
    $.post("/vocabularyAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newVocab = responseObject.newVocabModule;
            const newVocabModule = new Vocabulary(newVocab.id, newVocab.dateCreated, newVocab.dateModified, newVocab.term, newVocab.def);
            insertModule(newVocabModule);
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