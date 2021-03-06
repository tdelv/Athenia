<#-- TOP BAR -------------------------------------------------------------------->
<#assign topbar>

    <!-- Topbar -->
    <nav class="navbar navbar-expand navbar-light bg-white topbar mb-4 static-top shadow">


        <span id="currentLanguage">${currentLanguage}</span>


        <!-- Topbar Navbar -->
        <ul class="navbar-nav ml-auto">

            <!-- Nav Item - User Information -->
            <li class="nav-item dropdown no-arrow">
                <a class="nav-link dropdown-toggle" href="#" id="userDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <span class="d-lg-inline text-gray-600 small mr-2">${username}</span>
                    <i class="fas fa-user"></i>
                </a>
                <!-- Dropdown - User Information -->
                <div class="dropdown-menu dropdown-menu-right shadow animated--grow-in" aria-labelledby="userDropdown">
                    <a class="dropdown-item" href="#">
                        <i class="fas fa-user fa-sm fa-fw mr-2 text-gray-400"></i>
                        Profile
                    </a>
                    <a class="dropdown-item" href="#">
                        <i class="fas fa-cogs fa-sm fa-fw mr-2 text-gray-400"></i>
                        Settings
                    </a>
                    <div class="dropdown-divider"></div>
                    <a class="dropdown-item" href="#" data-toggle="modal" data-target="#logoutModal">
                        <i class="fas fa-sign-out-alt fa-sm fa-fw mr-2 text-gray-400"></i>
                        Logout
                    </a>
                </div>
            </li>

        </ul>

    </nav>
    <!-- End of Topbar -->

</#assign>

<#-- SIDE BAR -------------------------------------------------------------------->
<#assign sidebar>

    <!-- Sidebar -->
    <ul class="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">

        <!-- Sidebar Brand / Athenia Label -->
        <a class="sidebar-brand d-flex align-items-center justify-content-center" href="home">
            <div class="sidebar-brand-text mx-3">Athenia</div>
        </a>

        <!-- Divider -->
        <hr class="sidebar-divider my-0">

        <!-- Nav Item - Home -->
        <li class="nav-item active">
            <a class="nav-link" href="home">
                <i class="fas fa-fw fa-home"></i>
                <span>Home</span></a>
        </li>

        <!-- Divider -->
        <hr class="sidebar-divider">

        <!-- Heading -->
        <div class="sidebar-heading">
            User-Generated Content
        </div>

        <!-- Nav Item - Notes -->
        <li class="nav-item">
            <a class="nav-link collapsed" href="#" data-toggle="collapse" data-target="#collapseTwo" aria-expanded="true" aria-controls="collapseTwo">
                <i class="fas fa-fw fa-pencil-alt"></i>
                <span>Notes</span>
            </a>
            <div id="collapseTwo" class="collapse" aria-labelledby="headingTwo" data-parent="#accordionSidebar">
                <div class="bg-white py-2 collapse-inner rounded">
                    <a class="collapse-item newNoteButton" href="noteEditor?id=new&currentLanguage=${currentLanguage}">New Note</a>
                    <a class="collapse-item" href="notes?currentLanguage=${currentLanguage}">View Notes</a>
                </div>
            </div>
        </li>

        <!-- Divider -->
        <hr class="sidebar-divider">

        <!-- Heading -->
        <div class="sidebar-heading">
            Software-Generated Content
        </div>

        <!-- Nav Item - Vocabulary -->
        <li class="nav-item">
            <a class="nav-link" href="vocabulary?currentLanguage=${currentLanguage}">
                <i class="fas fa-fw fa-list"></i>
                <span>Vocabulary</span></a>
        </li>

        <!-- Nav Item - Conjugations -->
        <li class="nav-item">
            <a class="nav-link" href="conjugations?currentLanguage=${currentLanguage}">
                <i class="fas fa-fw fa-table"></i>
                <span>Conjugations</span></a>
        </li>

        <!-- Divider -->
        <hr class="sidebar-divider">

        <!-- Nav Item - Review Mode -->
        <!-- TODO: Differentiate this -->
        <li class="nav-item">
            <a class="nav-link" href="reviewMode?currentLanguage=${currentLanguage}">
                <i class="fas fa-fw fa-clipboard-list"></i>
                <span>Review Mode</span></a>
        </li>

    </ul>
    <!-- End of Sidebar -->

</#assign>

<#-- TEMPLATE CONTENT -------------------------------------------------------------------->
<#assign content>
    <!-- Page Wrapper -->
    <div id="wrapper">

        ${sidebar}

        <!-- Content Wrapper -->
        <div id="content-wrapper" class="d-flex flex-column">

            <!-- Main Content -->
            <div id="content">

                ${topbar}

                <!-- Begin Page Content -->
                <div class="container-fluid">

                    ${pagecontent}

                </div>
                <!-- End of Page Content -->

            </div>
            <!-- End of Main Content -->

            <!-- Footer -->
            <footer class="sticky-footer bg-white">
                <div class="container my-auto">
                    <div class="copyright text-center my-auto">
                        <span>This is the Footer Yeehaw</span>
                    </div>
                </div>
            </footer>
            <!-- End of Footer -->

        </div>
        <!-- Content Wrapper -->

    </div>
    <!-- End of Page Wrapper -->

    <!-- Logout Modal-->
    <div class="modal fade" id="logoutModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="exampleModalLabel">Ready to Leave?</h5>
                    <button class="close" type="button" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">×</span>
                    </button>
                </div>
                <div class="modal-body">Select "Logout" below if you are ready to end your current session.</div>
                <div class="modal-footer">
                    <button class="btn btn-secondary" type="button" data-dismiss="modal">Cancel</button>
                    <a class="btn btn-primary" href="logout">Logout</a>
                    <#--TODO: make it so that they actually get logged out of the google thing-->
                </div>
            </div>
        </div>
    </div>

</#assign>

<#include "main.ftl">