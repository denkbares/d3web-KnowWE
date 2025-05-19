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
import {Popup as ReactPopup} from "reactjs-popup";
import {mountRoot} from "./utils.ts";

export function mountPopup(props: PopupProps) {
    const [root, unmount] = mountRoot();
    root.render(<Popup {...props} unmount={unmount} />);
}

export type PopupProps = {
    title: string;
    message: React.ReactNode | string;
    button?: ButtonProps;
    secondaryButtons?: ButtonProps[];
};

export type ButtonProps = {
    label: string;
    action: (event: React.MouseEvent<HTMLButtonElement>) => Promise<void>;
    type: "Primary" | "Danger" | "Secondary";
}

function Popup({title, message, button, secondaryButtons, unmount}: PopupProps & {unmount: () => void}) {
    function convertTypeToClassName(type: ButtonProps["type"]) {
        if (type === "Primary") return "btn-default";
        if (type === "Danger") return "btn-danger";
        if (type === "Secondary") return "btn-secondary";
    }

    return (
        <ReactPopup position="center center" open={true} onClose={unmount} className={"snapshot-popup"}>
            <header>
                <div className={"buttons"}>
                    <button onClick={unmount}>
                        <i className="fa-regular fa-circle-xmark"></i>
                    </button>
                </div>
                <h2>{title}</h2>
            </header>

            <main>{message}</main>

            <footer>
                {button && <button className={"btn " + convertTypeToClassName(button.type)}
                                   onClick={button.action}>{button.label}</button>}
                {secondaryButtons && secondaryButtons.map(button =>
                    <button className={"btn " + convertTypeToClassName(button.type)}
                            onClick={button.action}>{button.label}</button>,
                )}
            </footer>
        </ReactPopup>
    );
}