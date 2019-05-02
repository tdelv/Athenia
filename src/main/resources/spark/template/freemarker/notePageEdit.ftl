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
                                <h1>${title}</h1>
                            </div>

                            <div class="row no-gutters align-items-center">
                                <h3>Date</h3>
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
                        <div class="card-body">
                            <div class="row no-gutters align-items-center">
                                <#--TODO: list modules -->
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- End of Note Body Row -->

        </div>
        <!-- End of Main Column -->

        <!-- Menu Column -->
        <div class="col">
            <div class="card border-left-warning shadow">
                <div class="card-body">

                    <div class="col">
                        <a href="#" class="btn btn-success btn-circle" id="insertVocabButton">
                            <i class="fa fa-list"></i>
                        </a>

                        <a href="#" class="btn btn-success btn-circle">
                            <i class="fa fa-list"></i>
                        </a>

                        <a href="#" class="btn btn-success btn-circle">
                            <i class="fa fa-list"></i>
                        </a>

                        <a href="#" class="btn btn-success btn-circle">
                            <i class="fa fa-list"></i>
                        </a>

                        <a href="#" class="btn btn-success btn-circle">
                            <i class="fa fa-list"></i>
                        </a>
                    </div>

                </div>
            </div>
        </div>
        <!-- End of Menu Column -->

    </div>
    <#-- End of Row to contain all of this page's content -->

</div>

</#assign>

<#assign script>
    <script src="js/notePage.js"></script>

</#assign>

<#include "template.ftl">