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

import React from "react";
import {mountRoot} from "./utils.ts";
import Popup from "reactjs-popup";

export default function mountMessagePopUp(props: MessagePopUpProps) {
    const [root, unmount] = mountRoot();
    const onClose = props.onClose;
    props.onClose = () => {
        if (onClose) {
            onClose();
        }
        unmount();
    };

    root.render(<MessagePopUp {...props} />);
}

type MessagePopUpProps = {
    title: string;
    message: React.ReactNode;
    onClose?: () => void;
};

function MessagePopUp({title, message, onClose}: MessagePopUpProps) {
    return (
        <Popup position="center center" open={true} onClose={onClose} className={"snapshot-popup"}>
            <header>{title}</header>

            <main>{message}</main>

            <footer>
                <button onClick={onClose}>Close</button>
            </footer>
        </Popup>
    );
}
