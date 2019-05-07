<#assign pagecontent>
<!-- Begin Page Content -->
<div class="container-fluid">

    <#-- Row to contain all of this page's content -->
    <div class="row">

        <!-- Main Column -->
        <div class="col-10">

            <!-- Note Heading Row -->
            <div class="row">

                <div class="col">
                    <div class="card border-bottom-primary shadow">
                        <div class="card-body">
                            <div class="row no-gutters align-items-center">
                                <input type="text" class="form-control-lg mb-3" id="notePageTitle" placeholder="${title}">
                            </div>

                            <div class="row no-gutters align-items-center">
                                <#--<h3>Date</h3>-->
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- End of Note Heading Row -->

            <!-- Note Body Row -->
            <div class="row">
                <div class="col">
                    <div class="card border-bottom-primary shadow" style="margin-top: 1vw;">
                        <div class="card-body" id="noteBody">

                        </div>
                    </div>
                </div>
            </div>
            <!-- End of Note Body Row -->

        </div>
        <!-- End of Main Column -->

        <!-- Menu Column -->
        <div class="col">
            <div class="card border-left-success shadow">
                <div class="card-body">

                    <div class="col d-flex flex-column">

                        <h6>Insert</h6>

                        <a href="#" class="btn btn-success btn-circle mb-2" id="insertTextButton">
                            <i class="fa fa-font"></i>
                        </a>

                        <a href="#" class="btn btn-success btn-circle mb-2" id="insertVocabButton">
                            <i class="fa fa-list"></i>
                        </a>

                        <a href="#" class="btn btn-success btn-circle mb-2" id="insertConjugationButton">
                            <i class="fa fa-table"></i>
                        </a>

                        <a href="#" class="btn btn-success btn-circle mb-2" id="insertExclamationButton">
                            <i class="fa fa-exclamation"></i>
                        </a>

                        <a href="#" class="btn btn-success btn-circle mb-2" id="insertQuestionButton">
                            <i class="fa fa-question"></i>
                        </a>

                    </div>

                </div>
            </div>
        </div>
        <!-- End of Menu Column -->

    </div>
    <#-- End of Row to contain all of this page's content -->

    <div class="invisible">${freeNoteId}</div>
    <div class="invisible" id="freeNote">${freeNote}</div>

</div>

</#assign>

<#assign script>
    <script src="js/notePage.js"></script>

</#assign>

<#include "template.ftl">