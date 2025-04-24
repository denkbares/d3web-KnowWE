import React, {useEffect, useState} from "react";
import {receiveJson} from "./utils.ts";
import deploySnapshotPopUp from "./DeploySnapshotPopUp.tsx";
import deleteSnapshotPopUp from "./DeleteSnapshotPopUp.tsx";
import {downloadSnapshot} from "./httpRequests.ts";

export default function SnapshotsTable() {
    const [snapshots, setSnapshots] = useState<SnapshotDTO[] | undefined>();
    const [error, setError] = useState<Error | undefined>();

    useEffect(() => {
        fetch("action/ListSnapshotsAction").then(receiveJson).then(setSnapshots).catch(setError);
    }, []);

    return (
        <table className={"snapshots-table"} style={{marginTop: "1rem"}}>
            <thead>
            <tr>
                <th>Name</th>
                {/*<th>Date</th>*/}
                {KNOWWE.core.util.isAdmin() === "true"
                    ? (<th>Action</th>)
                    : ''}
            </tr>
            </thead>
            <tbody>
            {error && (
                <tr>
                    <td colSpan={2}>{error.message}</td>
                </tr>
            )}
            {!snapshots && !error && (
                <tr>
                    <td colSpan={2}>
                        <div className={"versioning-spinner"} />
                    </td>
                </tr>
            )}
            {!error && snapshots && snapshots.length === 0 && (
                <tr>
                    <td colSpan={2}>No Snapshots yet.</td>
                </tr>
            )}
            {!error &&
                snapshots &&
                snapshots.length > 0 &&
                snapshots.map(snapshot => (
                    <tr>
                        <td title={snapshot.name + ".zip"}>{snapshot.name}</td>
                        {KNOWWE.core.util.isAdmin() === "true"
                            ? (<td>
                                {snapshot.type === "ATTACHMENT"
                                    ? <SnapshotPanelAttachmentButtons snapshot={snapshot} />
                                    : <SnapshotPanelTmpFileButtons snapshot={snapshot} />
                                }
                            </td>)
                            : ''}
                    </tr>
                ))}
            </tbody>
        </table>
    );
}

function SnapshotPanelAttachmentButtons({snapshot}: {snapshot: SnapshotDTO}) {
    return (
        <div style={{display: "flex", flexDirection: "row"}}>
            <button
                className={"btn btn-default"}
                title="Deploy this snapshot to the wiki"
                onClick={() =>
                    deploySnapshotPopUp({
                        snapshotName: snapshot.name,
                        snapshotType: snapshot.type,
                        snapshotPageName: snapshot.parent,
                    })
                }
            >
                Deploy
            </button>
            <a href={"Upload.jsp?page=" + snapshot.parent}>
                <button
                    className={"btn btn-default"}
                    title="Open a list of all attachments for this page"

                >
                    Open
                </button>
            </a>
        </div>
    );
}

function SnapshotPanelTmpFileButtons({snapshot}: {snapshot: SnapshotDTO}) {
    return (
        <div style={{display: "flex", flexDirection: "row"}}>
            <button
                className={"btn btn-default"}
                title="Deploy this snapshot to the wiki"
                onClick={() =>
                    deploySnapshotPopUp({
                        snapshotName: snapshot.name,
                        snapshotType: snapshot.type,
                        snapshotPageName: snapshot.parent,
                    })
                }
            >
                Deploy
            </button>
                <button
                    className={"btn btn-default"}
                    title="Download this snapshot"
                    onClick={() =>
                        downloadSnapshot(snapshot.path, snapshot.name)
                    }
                >
                    Download
                </button>
            <button
                className={"btn btn-default"}
                title="Delete this snapshot from the wiki permanently"
                onClick={() =>
                    deleteSnapshotPopUp({
                        snapshotPath: snapshot.path,
                        snapshotName: snapshot.name,
                    })
                }
            >
                Delete
            </button>
        </div>
    );
}
