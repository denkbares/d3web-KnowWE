import React from "react";
import Popup from "reactjs-popup";
import {mountRoot, PopUpProps, wrapFormSubmission} from "./utils.ts";
import {deploySnapshot} from "./httpRequests.ts";

export default function mountPublishChangesPopUp(props: DeploySnapshotPopupProps) {
    const [root, unmount] = mountRoot();
    root.render(<DeploySnapshotPopUp {...props} unmount={unmount} />);
}

type DeploySnapshotPopupProps = {
    snapshotName: string;
};

function DeploySnapshotPopUp({snapshotName, unmount}: PopUpProps & DeploySnapshotPopupProps) {

    return (
        <Popup position="center center" open={true} onClose={unmount} className={"snapshots-popup"}>
            <form>
                <header>Delete Snapshot</header>
                <main>
                    <div>
                        Are you sure you want to deploy <b>{snapshotName}</b>?
                    </div>
                    <footer>
                        <button onClick={wrapFormSubmission(() => deploySnapshot(snapshotName + ".zip"))}>
                            Deploy
                        </button>
                        <button onClick={unmount}>Cancel</button>
                    </footer>
                </main>
            </form>
        </Popup>
    );
}
