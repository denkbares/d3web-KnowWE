/*
 * Copyright (C) 2018 denkbares GmbH, Germany
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

KNOWWE = KNOWWE || {};
KNOWWE.core = KNOWWE.core || {};
KNOWWE.core.plugin = KNOWWE.core.plugin || {};

(function init() {
	window.addEvent('domready', _KL.setup);
	if (window.location.search !== "?page=Create%20new%20EDB%20entry") if (KNOWWE.helper.loadCheck(['Wiki.jsp'])) {
		window.addEvent('domready', function () {
			KNOWWE.core.plugin.dropZone.initAttachToExisting();
		});
	}
}());


/**
 * Namespace: KNOWWE.core.plugin.dropZone for debugging d3web expressions in KnowWE
 */
KNOWWE.core.plugin.dropZone = function () {

	function handleDragOver(event) {
		event.stopPropagation();
		event.preventDefault();
		setDragOverStyle(event.target);
		const dropIndicator = jq$(event.target).closest('.dropzone').find(".drop-indicator").first();
		if (dropIndicator.length === 0) return;
		dropIndicator[0].addEventListener('dragleave', handleDragLeave);
		event.dataTransfer.dropEffect = 'copy';
	}


	function handleDragLeave(event) {
		resetStyle(event.target);
	}

	function prepareUploadData(event, pageName) {
		event.stopPropagation();
		event.preventDefault();
		let form = jq$(event.target).closest('.dropzone').find('.drop-indicator').first();
		const ajaxData = new FormData();
		if (pageName == null) pageName = KNOWWE.helper.getPagename();
		ajaxData.append('page', pageName);
		ajaxData.append('nextpage', 'Upload.jsp?page=' + pageName);
		ajaxData.append('action', 'upload');

		let files = [];
		if (typeof event.dataTransfer !== "undefined") {
			files = event.dataTransfer.files; // Dropped files
		}
		const input = form.find('input[type="file"]').first();
		if (files.length === 0) {
			if (typeof input.context.files !== "undefined" && input.context.files.length > 0) {
				files.append(input.context.files); // Manually chosen files w/ file chooser
			} else {
				setClass(event.target, "uploading", "Not a valid attachment...");
				setTimeout(function () {
					resetStyle(event.target, "Drop attachment(s) here");
				}, 1000);
				return null;
			}
		}

		setUploadingStyle(event.target);
		Array.prototype.forEach.call(files, function (file) {
			ajaxData.append(input.attr('name'), file);
		});

		return {
			form: form,
			ajaxData: ajaxData
		}
	}

	function ajaxData(uploadData, event) {
		jq$.ajax({
			url: uploadData.form.attr('action'),
			type: uploadData.form.attr('method'),
			data: uploadData.ajaxData,
			cache: false,
			contentType: false,
			processData: false,
			success: function () {
				setUploadedStyle(event.target);
				if (!event.reload) {
					setTimeout(function () {
						resetStyle(event.target, "Drop attachment(s) here")
					}, 1000)
					if (event.callback) event.callback();
				} else {
					setTimeout(function () {
						window.location.reload();
					}, 1000);
				}
			},
			error: function (data) {
				KNOWWE.notification.error(data.responseText);
				resetStyle(event.target, "Drop attachment(s) here");
			}
		});ihreih
	}

	function handleDropToExisting(event) {
		event.reload = true;
		const uploadData = prepareUploadData(event)
		ajaxData(uploadData, event);
	}

	function setUploadingStyle(element) {
		setClass(element, "uploading", "Upload in progress...");
	}

	function setDragOverStyle(element) {
		setClass(element, "dragover", null);
	}

	function setUploadedStyle(element) {
		setClass(element, "uploaded", "Upload complete 🎉");
	}

	function resetStyle(element, title) {
		setClass(element, "", title);
	}

	function setClass(element, className, title) {
		const dropZone = jq$(element).closest('.dropzone');
		const boxInput = dropZone.find(".box-input").first();
		boxInput.attr('class', 'box-input');
		boxInput.addClass(className);
		const dropIndicator = dropZone.find('.drop-indicator').first();
		dropIndicator.attr('class', 'drop-indicator'); // Remove all other classes
		dropIndicator.addClass(className);
		const dropTextWrapper = dropIndicator.find("label").first();
		if (title !== null) dropTextWrapper.html(title);
		dropTextWrapper.removeClass()
		dropTextWrapper.addClass(className);
	}

	function attachDropZoneToElement(element, actionUrl, multiple, title, mode) {
		if (element == null || typeof element === "undefined" || element.length === 0) return;
		const form =
			'<form class="drop-indicator" method="post" action=' + actionUrl + ' enctype="multipart/form-data">' +
			'  <div class="box-input">' +
			'    <input style="display: none" class="box__file" type="file" name="files" id="file" ' +
			(multiple ? 'data-multiple-caption="{count} files selected" multiple' : '') + ' />' +
			'    <label for="file"><span class="box__dragndrop"/>' + title + '</span></label>' +
			'  </div>' +
			'</form>';

		if (mode === "full-height") {
			element.addClass("dropzone full-height");
			element.prepend(form);
		} else if (mode === "append") {
			element.append('<div class="dropzone">' + form + '</div>');
		} else if (mode === "replace") {
			element.addClass("dropzone replace");
			element.prepend(form);
		}
	}

	return {

		prepareUploadData(event, name) {
			return prepareUploadData(event, name);
		},

		uploadData(data, event) {
			ajaxData(data, event)
		},

		setDropZoneStyleUploading(element) {
			setUploadingStyle(element);
		},

		setDropZoneStyleUploaded(element) {
			setUploadedStyle(element)
		},

		resetDropZoneStyle(element, title) {
			resetStyle(element, title);
		},

		initAttachToExisting: function () {
			KNOWWE.core.plugin.dropZone.addDropZoneTo('div.page', "Drop attachment(s)", handleDropToExisting)
		},

		addDropZoneTo(elementSelector, title, dropHandlerCallback, actionUrl = 'attach', mode = "full-height", multiple = true) {
			const canWrite = jq$('#knowWEInfoCanWrite').attr('value');
			if (!KNOWWE.core.util.isHaddockTemplate() || typeof canWrite === 'undefined' || canWrite !== 'true') return;

			const element = jq$(elementSelector);
			attachDropZoneToElement(element, actionUrl, multiple, title, mode);

			for (const i in element) {
				if (!element.hasOwnProperty(i)) continue;
				if (!Number.isInteger(parseInt(i))) continue;
				element[i].addEventListener('dragover', handleDragOver);
				element[i].addEventListener('drop', dropHandlerCallback);
			}
			element.find('input').first().on('change', dropHandlerCallback);
		}
	}

}
();
