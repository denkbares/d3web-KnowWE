import {createRoot, Root} from "react-dom/client";
import mountMessagePopUp from "./MessagePopUp.tsx";
import React from "react";

export type PopUpProps = {
    unmount: () => void;
};

export function mountRoot(): [Root, () => void] {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);
    const unmount = () => {
        root.unmount();
        container.remove();
    };
    return [root, unmount];
}

export function wrapFormSubmission(callable: CallableFunction) {
    return async (event: React.MouseEvent<HTMLButtonElement>) => {
        event.preventDefault();
        try {
            await callable();
        } catch (e) {
            mountMessagePopUp({
                title: "Error",
                message: (e as Error).message,
            });
        }
    };
}

export async function receiveJson(response: Response): Promise<any> {
    const result = await response.json();
    if (isError(result) && result.status !== 200) {
        throw new Error(result.message);
    }
    return result;
}

export function isError(object: any): object is KnowWEError {
    return typeof object === "object" && "status" in object && "message" in object;
}

export function handleError(error: KnowWEError) {
    KNOWWE.editCommons.hideAjaxLoader();
    KNOWWE.notification.error("Error", error.message, Date.now(), 60000);
}

export async function downloadFile(data: Blob | MediaSource, filename: string) {
    const fileURL = URL.createObjectURL(data);
    // create a link and set its href to the temporary url
    const link = document.createElement("a");
    link.href = fileURL;
    link.setAttribute("download", filename);
    link.click();
    // clean up
    URL.revokeObjectURL(fileURL);
}
