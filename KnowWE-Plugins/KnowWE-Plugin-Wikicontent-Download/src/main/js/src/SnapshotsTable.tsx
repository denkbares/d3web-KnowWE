import React, {createContext, useContext, useEffect, useState} from "react";
import {receiveJson, wrapFormSubmission} from "./utils.ts";
import {deleteSnapshot, deploySnapshot, downloadSnapshot} from "./httpRequests.ts";
import {mountPopup} from "./Popup.tsx";

const Context = createContext({
    currentContextMenu: "",
    setCurrentContextMenu: (_contextMenu: string) => {
    },
});

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

    const snapshotsWithoutAutosaves = snapshots?.filter((snapshot: SnapshotDTO) => !snapshot.name.includes("AutosaveSnapshot"));
    const autosaveSnapshots = snapshots?.filter((snapshot: SnapshotDTO) => snapshot.name.includes("AutosaveSnapshot"));

    const [currentContextMenu, setCurrentContextMenu] = useState<string>("");

    return (
        <Context.Provider value={{currentContextMenu, setCurrentContextMenu}}>
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
                        <div className={"headline"}>
                            <h3>Snapshots</h3>
                            <div
                                className={"counter"}>
                                <span>{snapshotsWithoutAutosaves ? snapshotsWithoutAutosaves.length : 0}</span>
                            </div>
                        </div>
                        <SnapshotsPartialTable snapshots={snapshotsWithoutAutosaves} />
                        <div className={"headline"}>
                            <h3>Autosave Snapshots</h3>
                            <div
                                className={"counter"}>
                                <span>{autosaveSnapshots ? autosaveSnapshots.length : 0}</span>
                            </div>
                        </div>
                        <SnapshotsPartialTable snapshots={autosaveSnapshots} />
                    </div>
                )}
            </div>
        </Context.Provider>
    );
}

function SnapshotsPartialTable({snapshots}: {
    snapshots: SnapshotDTO[] | undefined
}) {

    if (snapshots === undefined) return null;

    const context = useContext(Context);

    return (
        <table className={"snapshots-table"}>
            <tbody>
            {
                snapshots.length > 0 &&
                snapshots.map(snapshot => (
                    <tr>
                        <td title={snapshot.name + ".zip"} className={"truncate"}>{snapshot.name}</td>
                        {KNOWWE.core.util.isAdmin() === "true" &&
                            (
                                <td>
                                    <div className={"context-menu relative"}>
                                        <button
                                            className={"btn btn-default context-menu"}
                                            onClick={() => (context.currentContextMenu === snapshot.path)
                                                ? context.setCurrentContextMenu("")
                                                : context.setCurrentContextMenu(snapshot.path)}
                                        >
                                            <i className="fa-regular fa-ellipsis-vertical"></i>
                                        </button>
                                        {context.currentContextMenu === snapshot.path &&
                                            <SnapshotContextMenu snapshot={snapshot} />}
                                    </div>
                                </td>
                            )}
                    </tr>
                ))}
            </tbody>
        </table>
    );
}

function SnapshotContextMenu({snapshot}: {snapshot: SnapshotDTO}) {
    const size = (snapshot.size / (1024 * 1024)).toFixed(2);
    return (
        <ul className={"options absolute"}>
            <li>
                <button title={`Download this snapshot (${size} MB)`}
                        onClick={() =>
                            downloadSnapshot(snapshot.path, snapshot.name)
                        }>
                    <i className="fa-regular fa-arrow-down-to-bracket"></i>Download
                </button>
            </li>
            <li>
                <button
                    title="Deploy this snapshot to the wiki"
                    onClick={() => deploySnapshotPopUp(snapshot.name)}>
                    <i className="fa-regular fa-rocket-launch"></i>Deploy
                </button>
            </li>
            <li>
                <button
                    title={"Delete this snapshot from the wiki permanently (" + size + " MB)"}
                    onClick={() =>
                        deleteSnapshotPopUp({
                            snapshotPath: snapshot.path,
                            snapshotName: snapshot.name,
                        })
                    }>
                    <i className="fa-regular fa-trash"></i>Delete
                </button>
            </li>
        </ul>
    );
}

function deleteSnapshotPopUp(props: {snapshotPath: string, snapshotName: string}) {
    mountPopup({
        title: "Delete",
        message: <p>Are you sure you want to delete <b>{props.snapshotName} ?</b></p>,
        button: {
            label: "Delete Snapshot",
            type: "Danger",
            action: wrapFormSubmission(() => deleteSnapshot(props.snapshotPath, props.snapshotName)),
        },
    });
}

function deploySnapshotPopUp(snapshotName: string) {
    mountPopup({
        title: "Deploy",
        message: <p>Are you sure you want to deploy <b>{snapshotName} to the Wiki ?</b></p>,
        button: {
            label: "Deploy Snapshot",
            type: "Primary",
            action: wrapFormSubmission(() => deploySnapshot(snapshotName + ".zip")),
        },
    });
}
