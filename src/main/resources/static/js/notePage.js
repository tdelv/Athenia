// TODO: for all add/delete/update post requests, send the current note id

let freeNoteId = null;

$( document ).ready(function() {

    let tt = new Tooltip($("#insertTextButton"), {
        placement: "right",
        title: "Top",
        trigger: "hover"
    });

    $("#insertTextButton").click(insertNote);
    $("#insertVocabButton").click(insertVocab);
    $("#insertConjugationButton").click(insertConjugation);
    $("#insertExclamationButton").click(insertExclamation);
    $("#insertQuestionButton").click(insertQuestion);
});

function getFreeNoteId() {

    if (freeNoteId == null) {
        freeNoteId = $(".invisible").html();
    }

    return freeNoteId;
}

function insertModule(module) {
    $("#noteBody").append(module.toHTML());
    module.setUp();
}

function insertNote() {
    console.log("inserting note");
    const postParameters = {noteString: "content", freeNoteId: getFreeNoteId()};
    $.post("noteAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newNote = responseObject.newNoteModule;
            let newNoteModule;
            newNoteModule = new Note(newNote.id, newNote.dateCreated, newNote.dateModified, newNote.noteContent);
            insertModule(newNoteModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}

function insertVocab() {
    console.log("inserting vocab");
    const postParameters = {newTerm: "term", newDef: "definition", freeNoteId: getFreeNoteId()};
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
    const postParameters = {};
    $.post("/conjugationAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newConjugation = responseObject.newConjugationModule;
            const newConjugationModule = new ConjugationTable(newConjugation.id, newConjugation.dateCreated, newConjugation.dateModified, newConjugation.header, new List(), 3);
            insertModule(newConjugationModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });

}

function insertExclamation() {
    console.log("inserting exclamation");
    const postParameters = {alertString: "content", freeNoteId: getFreeNoteId()};
    $.post("alertAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newExclamation = responseObject.newAlertModule;
            const newExclamationModule = new Exclamation(newExclamation.id, newExclamation.dateCreated, newExclamation.dateModified, newExclamation.alertContent);
            insertModule(newExclamationModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}

function insertQuestion() {
    console.log("inserting question");
    const postParameters = {questionString: "content", freeNoteId: getFreeNoteId()};
    $.post("questionAdd", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const newQuestion = responseObject.newQuestionModule;
            const newQuestionModule = new Question(newQuestion.id, newQuestion.dateCreated, newQuestion.dateModified, newQuestion.questionContent);
            insertModule(newQuestionModule);
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}