import React from "react";
import {createRoot} from "react-dom/client";
import {createSnapshot} from "./httpRequests.ts";
import SnapshotsTable from "./SnapshotsTable.tsx";

export default function mountPanel(container: HTMLElement) {
    const root = createRoot(container);

    root.render(<SnapshotsPanel />);
}

function SnapshotsPanel() {
    const urlParams = new URLSearchParams(window.location.search);
    const pageName = urlParams.get("page") || "";
    return (
        <div className={"snapshots-panel"}>
            <h3>Snapshot Management Panel</h3>
            <hr />
            {KNOWWE.core.util.isAdmin() === 'true'
                ? (
                    <div>
                        <button className={"btn btn-default"} onClick={createSnapshot}>
                            Create Snapshot
                        </button>
                        <a href={"Upload.jsp?page=" + pageName}>
                            <button className={"btn btn-default"}>
                                Upload Snapshot
                            </button>
                        </a>
                    </div>
                )
                : ''
            }
            <SnapshotsTable/>
        </div>
    );
}
