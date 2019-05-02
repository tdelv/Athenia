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

        </div>
        <!-- End of Main Column -->

        <!-- Menu Column -->
        <div class="col">
            <div class="card border-left-warning shadow">
                <div class="card-body">
                    <p>menu</p>
                    <p>menu</p>
                    <p>menu</p>
                    <p>menu</p>
                    <p>menu</p>
                    <p>menu</p>
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