import React from "react";
import Popup from "reactjs-popup";
import {mountRoot, PopUpProps, wrapFormSubmission} from "./utils.ts";
import {deleteSnapshot} from "./httpRequests.ts";

export default function mountDeleteSnapshotPopUp(props: DeleteSnapshotPopupProps) {
    const [root, unmount] = mountRoot();
    root.render(<DeleteSnapshotPopUp {...props} unmount={unmount} />);
}

type DeleteSnapshotPopupProps = {
    snapshotPath: string;
    snapshotName: string;
};

function DeleteSnapshotPopUp({snapshotPath, snapshotName, unmount}: PopUpProps & DeleteSnapshotPopupProps) {
    return (
        <Popup position="center center" open={true} onClose={unmount} className={"snapshots-popup"}>
            <form>
                <header>Delete Snapshot</header>
                <main>
                    <div>
                        Are you sure you want to delete <b>{snapshotName}</b>?
                    </div>
                    <footer>
                        <button className="btn-danger"
                            onClick={wrapFormSubmission(() => deleteSnapshot(snapshotPath, snapshotName))}
                        >
                            Delete
                        </button>
                        <button onClick={unmount}>Cancel</button>
                    </footer>
                </main>
            </form>
        </Popup>
    );
}
