<#assign pagecontent>

    <div class="container-fluid">

        <div class="d-sm-flex align-items-center justify-content-between mb-4">
            <h1 class="h3 mb-0 text-gray-800">Review</h1>
        </div>

        <div class="row">
            <div class="col">
                <form id="reviewForm" name="reviewForm">
                    <div class="form-group container-fluid">

                        <div class="row align-items-center">

                            <div class="col">
                                <div class="mb-2">
                                    <label for="startDate">Start date:</label>
                                    <input type="date" id="startDate" name="startDate" style="width: 100%; border-radius: 7px">
                                </div>
                                <div>
                                    <label for="endDate">End date:</label>
                                    <input type="date" id="endDate" name="endDate" style="width: 100%; border-radius: 7px">
                                </div>
                            </div>

                            <div class="col">
                                <label for="tagSelection">Select which tags to review</label>
                                <select multiple="multiple" class="form-control" name="tagSelection" id="tagSelection">
                                    <#list allTags as tag>
                                        <option>${tag}</option>
                                    </#list>
                                </select>
                            </div>

                            <div class="col d-flex justify-content-center">
                                <#--<input type="submit" class="btn btn-primary" style="width:100%" value="Begin Reviewing">-->
                                <button class="btn btn-primary" id="submitButton" style="width:100%">
                                    Begin Reviewing
                                </button>
                            </div>

                        </div>

                    </div>

                </form>
            </div>
        </div>

    </div>

</#assign>

<#assign script>
    <script src="js/review.js"></script>
</#assign>

<#include "template.ftl">