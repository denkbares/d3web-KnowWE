import React, {useEffect, useState} from "react";
import deploySnapshotPopUp from "./DeploySnapshotPopUp.tsx";
import deleteSnapshotPopUp from "./DeleteSnapshotPopUp.tsx";
import {receiveJson} from "./utils.ts";
import {downloadSnapshot} from "./httpRequests.ts";

export default function SnapshotsTable() {

    const [snapshots, setSnapshots] = useState<SnapshotDTO[] | undefined>();
    const [error, setError] = useState<Error | undefined>();

    useEffect(() => {
        fetch("action/ListSnapshotsAction").then(receiveJson).then(setSnapshots).catch(setError);
    }, []);

    if (error) {
        return (
            <div><p>{error.message}</p></div>
        );
    }

    return (
        <div>
            {!snapshots && (
                <div>
                    <div className={"versioning-spinner"} />
                </div>
            )}
            {snapshots && snapshots.length === 0 && (
                <p>No Snapshots yet.</p>
            )}
            {snapshots && snapshots.length > 0 && (
                <div>
                    <SnapshotsPartialTable snapshots={snapshots}
                                           filter={(snapshot: SnapshotDTO) => !snapshot.name.startsWith("AutosaveSnapshot")} />
                    <h5>Autosaves</h5>
                    <SnapshotsPartialTable snapshots={snapshots}
                                           filter={(snapshot: SnapshotDTO) => snapshot.name.startsWith("AutosaveSnapshot")} />
                </div>
            )}
        </div>
    );
}

function SnapshotsPartialTable({snapshots, filter}: {
    snapshots: SnapshotDTO[],
    filter?: (snapshot: SnapshotDTO) => boolean
}) {
    return (
        <table className={"snapshots-table"}>
            <thead>
            <tr>
                <th>Name</th>
                {KNOWWE.core.util.isAdmin() === "true"
                    ? (<th>Action</th>)
                    : ""}
            </tr>
            </thead>
            <tbody>
            {
                snapshots.length > 0 &&
                snapshots.filter(s => filter ? filter(s) : s).map(snapshot => (
                    <tr>
                        <td title={snapshot.name + ".zip"}>{snapshot.name}</td>
                        {KNOWWE.core.util.isAdmin() === "true"
                            ? (<td>
                                <SnapshotPanelButtons snapshot={snapshot} />
                            </td>)
                            : ""}
                    </tr>
                ))}
            </tbody>
        </table>
    );
}

function SnapshotPanelButtons({snapshot}: {snapshot: SnapshotDTO}) {
    const size = (snapshot.size / (1024 * 1024)).toFixed(2);
    return (
        <div style={{display: "flex", flexDirection: "row"}}>
            <button
                className={"btn btn-default"}
                title="Deploy this snapshot to the wiki"
                onClick={() =>
                    deploySnapshotPopUp({
                        snapshotName: snapshot.name,
                    })
                }
            >
                Deploy
            </button>
            <button
                className={"btn btn-default"}
                title={`Download this snapshot (${size} MB)`}
                onClick={() =>
                    downloadSnapshot(snapshot.path, snapshot.name)
                }
            >
                Download
            </button>
            <button
                className={"btn btn-default"}
                title={`Delete
                this snapshot from the wiki permanently (
                ${size}
                MB
                )`}
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
