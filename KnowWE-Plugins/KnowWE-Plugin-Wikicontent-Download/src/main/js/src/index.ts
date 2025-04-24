import "./index.css";
import mountPanel from "./SnapshotsPanel.tsx";

jq$(() => {
    jq$("[data-name='SnapshotControlPanel'] .markupText").each((_, element) => mountPanel(element));
});
