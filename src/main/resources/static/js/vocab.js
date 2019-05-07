$( document ).ready(function() {
    const vocabList = $("#vocabContent").html();
    getVocabList(vocabList);

    $("#addVocab").on("click", function(e) {
        e.preventDefault();
        insertVocab();
    })

});



function getVocabList() {
    const postParameters = {};
    $.post("getVocabList", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);

        console.log("vocab response: " + responseObject);

        if (responseObject.successful) {
            const vocabContent = responseObject.vocabContent;
            for (let i = 0; i < vocabContent.length; i++) {
                const currVocab = vocabContent[i];
                let newVocab;
                if (moduleMap.has(currVocab.id)) {
                    newVocab = moduleMap.get(currVocab.id);
                } else {
                    console.log(currVocab);
                    console.log(currVocab.rating);
                    newVocab = new Vocabulary(currVocab);
                    moduleMap.set(currVocab.id, newVocab);
                }
                const newVocabHTML = newVocab.toHTML();
                $("#vocabularyContainer").append(newVocabHTML);
                newVocab.setUp();
            }
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
            const newVocabModule = new Vocabulary(newVocab);
            const newVocabModuleHTML = newVocabModule.toHTML();
            $("#vocabularyContainer").prepend(newVocabModuleHTML);
            newVocabModule.setUp();
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}