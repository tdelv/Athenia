<#assign pagecontent>
    <!-- Begin Page Content -->
    <div class="container-fluid">

        <!-- Page Heading -->
        <div class="d-sm-flex align-items-center justify-content-between mb-4">
            <h1 class="h3 mb-0 text-gray-800">Dashboard</h1>
            <!-- <a href="#" class="d-none d-sm-inline-block btn btn-sm btn-primary shadow-sm"><i class="fas fa-download fa-sm text-white-50"></i> Yeehaw</a> -->
        </div>

        <!-- Content Row -->
        <div class="row">

            <!-- Card - Notes -->
            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-primary shadow h-100 py-2" id="noteCard">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="h6 mb-0 font-weight-bold text-gray-800">Notes</div>
                                <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">${noteCount} Note Pages</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-pencil-alt fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Card - Vocabulary -->
            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-success shadow h-100 py-2" id="vocabCard">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="h6 mb-0 font-weight-bold text-gray-800">Vocabulary</div>
                                <div class="text-xs font-weight-bold text-success text-uppercase mb-1">${vocabCount} Vocab Words</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-list fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Card - Conjugations -->
            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-info shadow h-100 py-2" id="conjugationCard">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="h6 mb-0 mr-3 font-weight-bold text-gray-800">Conjugations</div>
                                <div class="text-xs font-weight-bold text-info text-uppercase mb-1">${conjugationCount} Words Conjugated</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-table fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Card - Review -->
            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-warning shadow h-100 py-2" id="reviewCard">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="h6 mb-0 font-weight-bold text-gray-800">Review</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-clipboard-list fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Content Row -->
        <div class="row">
        </div>

    </div>
    <!-- /.container-fluid -->
</#assign>

<#assign script>
    <script src="js/home.js"></script>

</#assign>

<#include "template.ftl">