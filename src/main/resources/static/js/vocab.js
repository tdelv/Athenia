$( document ).ready(function() {
    const vocabList = $("#vocabContent").html();
    getVocabList(vocabList);
});

function getVocabList() {
    const postParameters = {};
    $.post("getVocabList", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);
        if (responseObject.successful) {
            const vocabContent = responseObject.vocabContent;
            for (let i = 0; i < vocabContent.length; i++) {
                const currVocab = vocabContent[i];
                let newVocab;
                if (moduleMap.has(currVocab.id)) {
                    newVocab = moduleMap.get(currVocab.id);
                } else {
                    newVocab = new Vocabulary(currVocab.id, currVocab.dateCreated, currVocab.dateModified, currVocab.term, currVocab.def);
                }
                $("#vocabularyContainer").append(newVocab.toHTML());
            }
        } else {
            console.log("message: " + responseObject.message);
        }
    });
}