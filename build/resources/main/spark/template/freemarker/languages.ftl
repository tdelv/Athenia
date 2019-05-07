<#assign content>

    <!-- Page Wrapper -->
    <div class="d-flex justify-content-center" id="wrapper">

        <div class="col-lg-8" style="margin-top: 5vh;">
            <h1>Which language are you studying today?</h1>

            <div class="row d-flex" style="margin-top: 5vh;">

                <#list languages as l>
                    <div class="card mb-4" style="margin-right: 1vw;">
                        <div class="card-body languageCard">
                            <span class="langSpan">${l}</span>
                        </div>
                    </div>
                </#list>


                <a href="#" class="btn btn-primary btn-circle btn-lg" data-toggle="modal" data-target="#addLanguageModal" style="margin-right: 1vw;">
                    <i class="fa fa-plus"></i>
                </a>

                <a href="#" class="btn btn-danger btn-circle btn-lg" data-toggle="modal" data-target="#removeLanguageModal">
                    <i class="fa fa-minus"></i>
                </a>

            </div>


            <div class="modal fade show" id="addLanguageModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" style="padding-right: 15px; display: none;" aria-modal="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="exampleModalLabel">Add a Language</h5>
                            <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">×</span>
                            </button>
                        </div>

                        <div class="modal-body">
                            <input type="text" placeholder="language" id="languageInput"></input>
                        </div>

                        <div class="modal-footer">
                            <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
                            <a class="btn btn-primary" href="#" id="addLanguageButton">Add</a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal fade show" id="removeLanguageModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" style="padding-right: 15px; display: none;" aria-modal="true">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="exampleModalLabel">Which language would you like to remove?</h5>
                            <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">×</span>
                            </button>
                        </div>

                        <div class="modal-body">
                            <#list languages as l>
                                <a href="#" class="btn btn-primary btn-icon-split languageSelectForRemoveButton">
                                    <span class="text">${l}</span>
                                </a>
                            </#list>
                        </div>

                        <div class="modal-footer">
                            <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
                            <a class="btn btn-danger" href="#" id="removeLanguageButton">Remove</a>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>

</#assign>

<#assign script>
    <script src="js/languages.js"></script>
</#assign>

<#include "main.ftl">