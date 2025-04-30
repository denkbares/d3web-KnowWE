/**
 * Front-end KnowWE globals
 */
declare global {
    const KNOWWE: any;
    const Wiki: any;
    const jq$: JQueryStatic;

    type KnowWEError = {
        status: number;
        timestamp: number;
        message: string;
    };
}

/**
 * Snapshot specific API.
 */
declare global {
    type SnapshotDTO = {
        name: string;
        parent: string;
        path: string;
        size: number;
        type: SnapshotType;
    };

    type SnapshotType =
        | "ATTACHMENT"
        | "TMP_FILE";
}

export {};
