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
    snapshotPageName: string;
    snapshotType: SnapshotType;
};

function DeploySnapshotPopUp({snapshotName, snapshotPageName, snapshotType, unmount}: PopUpProps & DeploySnapshotPopupProps) {

    return (
        <Popup position="center center" open={true} onClose={unmount} className={"snapshots-popup"}>
            <form>
                <header>Delete Snapshot</header>
                <main>
                    <div>
                        Are you sure you want to deploy <b>{snapshotName}</b>?
                    </div>
                    <footer>
                        <button className=""
                                onClick={wrapFormSubmission(() => deploySnapshot(snapshotName + ".zip", snapshotType, snapshotPageName))}
                        >
                            Deploy
                        </button>
                        <button onClick={unmount}>Cancel</button>
                    </footer>
                </main>
            </form>
        </Popup>
    );
}
