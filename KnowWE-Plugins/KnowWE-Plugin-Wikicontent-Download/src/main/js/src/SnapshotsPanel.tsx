import React, {ChangeEvent} from "react";
import {createRoot} from "react-dom/client";
import {createSnapshot, uploadSnapshot} from "./httpRequests.ts";
import SnapshotsTable from "./SnapshotsTable.tsx";
import mountMessagePopUp from "./MessagePopUp.tsx";

export default function mountPanel(container: HTMLElement) {
    const root = createRoot(container);

    root.render(<SnapshotsPanel />);
}

function SnapshotsPanel() {
    return (
        <div className={"snapshots-panel"}>
            {KNOWWE.core.util.isAdmin() === "true"
                ? (
                    <div className={"buttons"}>
                        <button className={"btn btn-default"} onClick={createSnapshot}>
                            Create Snapshot
                        </button>
                        <div className="upload-button">
                            <label className="btn btn-default">Upload Snapshot</label>
                            <input type="file" name="file" onChange={handleUpload} />
                        </div>
                    </div>
                )
                : ""
            }
            <SnapshotsTable />
        </div>
    );
}

async function handleUpload(event: ChangeEvent) {
    try {
        const input = event.target as HTMLInputElement;
        if (typeof input.files === "undefined" || !input.files) return;
        if (input.files.length === 0) return;

        const file = input.files[0];
        await uploadSnapshot(file);
        window.location.reload();
    } catch (e) {
        mountMessagePopUp({
            title: "Error",
            message: (e as Error).message,
        });
    }

}