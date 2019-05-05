$(document).ready(function() {
    const conjugationList = $("#conjugationContainer").html();
    getConjugationList();

    $saveButtons = $(".conjSave");

});



function getConjugationList() {
    const postParameters = {};
    $.post("getConjugationList", postParameters, responseJSON => {
        const responseObject = JSON.parse(responseJSON);

        // console.log(responseObject);

        if (responseObject.successful) {

            const conjugationContent = responseObject.conjugationContent;
            // console.log("logging conjugationContent");
            // console.log(conjugationContent);

            for (let i = 0; i < conjugationContent.length; i++) {
                const currConj = conjugationContent[i];
                // console.log("logging currConj");
                // console.log(currConj);
                let newConj;
                if (moduleMap.has(currConj.id)) {
                    newConj = moduleMap.get(currConj.id);
                } else {
                    newConj = new ConjugationTable(currConj.id, currConj.dateCreated,
                        currConj.dateModified, currConj.header, currConj.tableContent);

                    moduleMap.set(currConj.id, newConj);
                }

                $("#conjugationContainer").append(newConj.toHTML());

            }

        } else {
            console.log("message: " + responseObject.message);
        }
    });
}

