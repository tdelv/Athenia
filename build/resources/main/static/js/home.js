$( document ).ready(function() {

    $("#noteCard").hover(mouseEnter, mouseLeave);
    $("#vocabCard").hover(mouseEnter, mouseLeave);
    $("#conjugationCard").hover(mouseEnter, mouseLeave);
    $("#reviewCard").hover(mouseEnter, mouseLeave);

    $("#noteCard").click(navToNotes);
    $("#vocabCard").click(navToVocab);
    $("#conjugationCard").click(navToConjugations);
    $("#reviewCard").click(navToReview);
});

function mouseEnter() {
    document.body.style.cursor = "pointer";
}

function mouseLeave() {
    document.body.style.cursor = "default";
}

function navToNotes() {
    // generates the address to navigate to
    const location = "notes";
    // navigates there
    window.location = location;
}

function navToVocab() {
    // generates the address to navigate to
    const location = "vocabulary";
    // navigates there
    window.location = location;
}

function navToConjugations() {
    // generates the address to navigate to
    const location = "conjugations";
    // navigates there
    window.location = location;
}

function navToReview() {
    // generates the address to navigate to
    const location = "reviewMode";
    // navigates there
    window.location = location;
}