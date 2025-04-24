/*
 * Copyright (C) 2025 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

import {downloadFile} from "./utils.ts";

export async function createSnapshot() {
    KNOWWE.editCommons.showAjaxLoader();
    const response = await fetch(
        `action/CreateSnapshotAction`
    );
    KNOWWE.editCommons.hideAjaxLoader();

    if (response.status !== 200) {
        const result = (await response.json()) as KnowWEError;
        throw new Error(result.message);
    }

    window.location.reload();
}


export async function deploySnapshot(
    name: string,
    type : SnapshotType,
    pageName?: string
) {

    KNOWWE.editCommons.showAjaxLoader();

    let response;
    if (type === "ATTACHMENT") {
        response = await fetch(
            `action/DeployAttachmentSnapshotAction?deploy_file=${name}&KWiki_Topic=${pageName}`
        );
    } else {
        response = await fetch(
            `action/DeployRepoSnapshotAction?deploy_file=${name}`
        );
    }

    KNOWWE.editCommons.hideAjaxLoader();

    if (response.status !== 200) {
        const result = (await response.json()) as KnowWEError;
        throw new Error(result.message);
    }

    window.location.reload();
}

export async function downloadSnapshot(
    path: string,
    name: string
) {

    KNOWWE.editCommons.showAjaxLoader();
    const response = await fetch(
        `action/DownloadFileAction?file=${path}&delete=false`
    );
    await downloadFile(await response.blob(), name + ".zip");
    KNOWWE.editCommons.hideAjaxLoader();

    if (response.status !== 200) {
        const result = (await response.json()) as KnowWEError;
        throw new Error(result.message);
    }

}

export async function deleteSnapshot(
    path: string,
    name: string
) {

    KNOWWE.editCommons.showAjaxLoader();
    const response = await fetch(
        `action/DownloadFileAction?file=${path}&delete=true`
    );
    await downloadFile(await response.blob(), name + ".zip");
    KNOWWE.editCommons.hideAjaxLoader();

    if (response.status !== 200) {
        const result = (await response.json()) as KnowWEError;
        throw new Error(result.message);
    }

    window.location.reload();
}
