
/**
 * Namespace: KNOWWE.tablesorter
 * The KNOWWE table sorter namespace.
 * Contains functions to sort HTMLTables.
 */
KNOWWE.tablesorter = function(){
    /*sorting function for strings */
    function stringSort(el1, el2){
        var cellOne = el1.getElementsByTagName("td")[col].innerHTML;
        var cellTwo = el2.getElementsByTagName("td")[col].innerHTML;

        return (cellOne > cellTwo) ? direction :(cellOne < cellTwo)? -direction : 0;
    }

    /*sorting function for integers */
    function intSort(el1, el2){
        var cellOne = parseInt(el1.getElementsByTagName("td")[col].innerHTML);
        var cellTwo = parseInt(el2.getElementsByTagName("td")[col].innerHTML);

        return (cellOne > cellTwo) ? direction :(cellOne < cellTwo)? -direction : 0;
    }
    return {
        /**
         * Function: init
         * Initializes the sort ability.
         *
         * Parameters:
         *     columns - The columns of the to sort table.
         *     tableID - The id of the table.
         */
        init : function(columns, tableID){
            if(!_KS('#' + tableID)) return;
            var tblHeader = document.getElementById(tableID).getElementsByTagName('thead')[0].getElementsByTagName('th');
            for( var i = 0; i < tblHeader.length; i++){
                if(columns[i].sortable == "true"){
                    var text = tblHeader[i].innerHTML;
                    _KE.add('click', tblHeader[i], function(){
                        KNOWWE.tablesorter.sort(i, tableID);
                    });
                }
            }
        },
        /**
         * Function: sort
         * Sorts the table according to the selected column.
         *
         * Parameters:
         *     columns - The columns of the to sort table.
         *     tableID - The id of the table.
         */
        sort : function(columnID, tableID){
            var tblHeader = document.getElementById(tableID).getElementsByTagName('thead')[0].getElementsByTagName('th');
            var tbody = document.getElementById(tableID).getElementsByTagName('tbody');

            var sortingType; var direction;
            var rowsSort = [];

            /* choose sorting type [asc desc]*/
            if(tblHeader[columnID].classname == "asc"){
                sortingType = "des";
                direction = -1;
            }else if(tblHeader[columnID].classname == "des"){
                sortingType = "asc";
                direction = 1;
            }else{
                sortingType = "asc";
                direction = 1;
            }

            /* for each tbody if query is found in more than one article*/
            for(var i = 0; i < tbody.length; i++){
                var rows = tbody[i].getElementsByTagName('tr');
                col = columnID;

                /* clone original nodes´, necessary for comparision. */
                for(var j = 0; j < rows.length; j++){
                    rowsSort[j] = rows[j].cloneNode(true);
                }

                /* sort the table*/
                rowsSort.sort(stringSort);

                /* replace old table with new sorted one */
                for(var k = 0; k < rows.length; k++){
                    rows[k].parentNode.replaceChild(rowsSort[k], rows[k]);
                }
            }

            /* store current sorting type */
            tblHeader[columnID].classname = sortingType;
        }
    }
}();

KNOWWE.tableUploadExcel = function () {

    // Private helper: actual upload
    async function handleFile(file, sectionID) {
        if (!file.name.match(/\.xlsx$/i)) {
            alert("Only XLSX files are allowed.");
            return;
        }

        try {
            await uploadFileToAction(file, sectionID);
            closeDialog();
        } catch (err) {
            alert(err.message || "Upload failed");
        }
    }

    async function uploadFileToAction(file, sectionID) {
        KNOWWE.editCommons.showAjaxLoader();

        const formData = new FormData();
        formData.append("file", file);

        await fetch(
          `action/ExcelToTableAction?SectionID=${encodeURIComponent(sectionID)}&X-XSRF-TOKEN=${Wiki.CsrfProtection}`,
          {
              method: "POST",
              body: formData,
          }
        );

        KNOWWE.editCommons.hideAjaxLoader();
        window.location.reload();
    }

    function closeDialog() {
        if (overlay && overlay.parentNode) {
            overlay.parentNode.removeChild(overlay);
            overlay = null;
        }
    }

    function showUploading(dropZone) {
        dropZone.innerHTML = `
        <div style="display:flex;flex-direction:column;align-items:center;gap:12px">
            <div class="kw-spinner"></div>
            <div style="font-size:14px;color:#555">Uploading Excel file…</div>
        </div>
    `;
        dropZone.style.borderColor = "#3b82f6";
        dropZone.style.color = "#3b82f6";
        dropZone.style.pointerEvents = "none";
    }


    let overlay;

    function open(sectionID) {
        overlay = document.createElement("div");
        overlay.style.position = "fixed";
        overlay.style.top = "0";
        overlay.style.left = "0";
        overlay.style.width = "100%";
        overlay.style.height = "100%";
        overlay.style.background = "rgba(0,0,0,0.4)";
        overlay.style.zIndex = "10000";
        overlay.style.display = "flex";
        overlay.style.alignItems = "center";
        overlay.style.justifyContent = "center";

        const dialog = document.createElement("div");
        dialog.style.background = "#fff";
        dialog.style.borderRadius = "8px";
        dialog.style.padding = "24px";
        dialog.style.width = "420px";
        dialog.style.textAlign = "center";
        dialog.style.boxShadow = "0 8px 24px rgba(0,0,0,0.2)";
        dialog.innerHTML = `
            <h3 style="margin-top:0">Upload Excel File</h3>
            <div id="dropZone"
                 style="
                    border: 2px dashed #aaa;
                    border-radius: 6px;
                    padding: 40px;
                    color: #666;">
                Drag & drop an XLSX file here
            </div>
            <div style="margin-top:16px">
                <button id="cancelBtn">Cancel</button>
            </div>
        `;

        overlay.appendChild(dialog);

        if (!document.getElementById("kw-spinner-style")) {
                const style = document.createElement("style");
                style.id = "kw-spinner-style";
                style.textContent = `
            .kw-spinner {
                width: 32px;
                height: 32px;
                border: 3px solid #dbeafe;
                border-top: 3px solid #3b82f6;
                border-radius: 50%;
                animation: kw-spin 0.8s linear infinite;
            }
    
            @keyframes kw-spin {
                to { transform: rotate(360deg); }
            }
            `;
                document.head.appendChild(style);
        }


        document.body.appendChild(overlay);

        const dropZone = dialog.querySelector("#dropZone");
        const cancelBtn = dialog.querySelector("#cancelBtn");

        cancelBtn.onclick = closeDialog;
        overlay.onclick = (e) => e.target === overlay && closeDialog();

        // Drag & drop handling
        dropZone.ondragover = (e) => {
            e.preventDefault();
            dropZone.style.borderColor = "#3b82f6";
            dropZone.style.color = "#3b82f6";
        };

        dropZone.ondragleave = () => {
            dropZone.style.borderColor = "#aaa";
            dropZone.style.color = "#666";
        };

        dropZone.ondrop = async (e) => {
            e.preventDefault();
            dropZone.style.borderColor = "#aaa";
            dropZone.style.color = "#666";

            if (e.dataTransfer.files.length > 0) {
                const file = e.dataTransfer.files[0];
                showUploading(dropZone);
                await handleFile(file, sectionID);
            }
        };

    }

    return { open };

}();




/* ############################################################### */
/* ------------- Onload Events  ---------------------------------- */
/* ############################################################### */
(function init(){

    window.addEvent( 'domready', _KL.setup );

    if( KNOWWE.helper.loadCheck( ['Wiki.jsp'] )){
        window.addEvent( 'domready', function(){
            KNOWWE.tablesorter.init();
        });
    };
}());