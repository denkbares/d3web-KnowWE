window.addEvent('domready', function() {

	// Use this method to add new controls (positions absolute to div #wisec-ranking-form)
	function views() {
	    return [  
	  
			// Scope
			{ view: 'Label',    rect: '10 0 100 24', anchors: 'left top', html: '<h3>Ranking Configuration</h3>' },
			{ view: 'Label',    rect: '15 45 100 24', anchors: 'left top', html: '<strong>Scope</strong> - items to consider in ranking' },
			{ view: 'Checkbox', rect: '10 70 24 24',  anchors: 'left top', checked: true },
        	{ view: 'Label',    rect: '40 70 100 24', anchors: 'left top', html: 'Substances' },
        	{ view: 'Checkbox', rect: '10 95 24 24',  anchors: 'left top', checked: false },
        	{ view: 'Label',    rect: '40 95 100 24', anchors: 'left top', text: 'Groups' },
			
			// View
			{ view: 'Label',    rect: '15 125 100 24', anchors: 'left top', html: '<strong>View</strong> - show only...' },
			{ view: 'Checkbox', rect: '10 150 24 24',  anchors: 'left top', checked: true },
        	{ view: 'Label',    rect: '40 150 100 24', anchors: 'left top', html: 'Active substances and groups' },
        	{ view: 'Checkbox', rect: '10 175 24 24',  anchors: 'left top', checked: false },
        	{ view: 'Label',    rect: '40 175 100 24', anchors: 'left top', text: 'Team' },

			// Criterias
			{ view: 'Label',    rect: '15 210 100 24', anchors: 'left top', html: '<strong>Criterias</strong>' },
			// Hazardous Properties
			{ view: 'Label',    rect: '15 235 100 24', anchors: 'left top', html: '<strong>Hazardous Properties</strong>' },
			{ view: 'Label', rect: '15 260 70 18', anchors: 'left top', text: 'CMR' },
			{ view: 'Slider', rect: '180 260 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 260 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 285 70 18', anchors: 'left top', text: 'Persistence' },
			{ view: 'Slider', rect: '180 285 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 285 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 310 70 18', anchors: 'left top', text: 'Bioakumulation_Potential' },
			{ view: 'Slider', rect: '180 310 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 310 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 335 70 18', anchors: 'left top', text: 'Aqua_Tox' },
			{ view: 'Slider', rect: '180 335 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 335 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 360 70 18', anchors: 'left top', text: 'EDC' },
			{ view: 'Slider', rect: '180 360 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 360 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 385 70 18', anchors: 'left top', text: 'Further_Tox' },
			{ view: 'Slider', rect: '180 385 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 385 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 410 70 18', anchors: 'left top', text: 'Climatic_Change' },
			{ view: 'Slider', rect: '180 410 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 410 90 18', anchors: 'right top' },
			// Mobility
			{ view: 'Label',    rect: '15 435 100 24', anchors: 'left top', html: '<strong>Mobility</strong>' },
			{ view: 'Label', rect: '15 460 70 18', anchors: 'left top', text: 'LRT' },
			{ view: 'Slider', rect: '180 460 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 460 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 485 70 18', anchors: 'left top', text: 'Water_solubility' },
			{ view: 'Slider', rect: '180 485 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 485 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 510 70 18', anchors: 'left top', text: 'Adsorption' },
			{ view: 'Slider', rect: '180 510 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 510 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 535 70 18', anchors: 'left top', text: 'Vapour_pressure' },
			{ view: 'Slider', rect: '180 535 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 535 90 18', anchors: 'right top' },
			// Exposure / Monitoring
			{ view: 'Label',    rect: '15 560 100 24', anchors: 'left top', html: '<strong>Exposure / Monitoring</strong>' },
			{ view: 'Label', rect: '15 585 70 18', anchors: 'left top', text: 'Air' },
			{ view: 'Slider', rect: '180 585 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 585 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 610 70 18', anchors: 'left top', text: 'Soil' },
			{ view: 'Slider', rect: '180 610 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 610 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 635 70 18', anchors: 'left top', text: 'Sediment' },
			{ view: 'Slider', rect: '180 635 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 635 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 660 70 18', anchors: 'left top', text: 'Surface_water' },
			{ view: 'Slider', rect: '180 660 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 660 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 685 70 18', anchors: 'left top', text: 'Sea' },
			{ view: 'Slider', rect: '180 685 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 685 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 710 70 18', anchors: 'left top', text: 'Groundwater' },
			{ view: 'Slider', rect: '180 710 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 710 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 735 70 18', anchors: 'left top', text: 'Drinking_water' },
			{ view: 'Slider', rect: '180 735 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 735 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 760 70 18', anchors: 'left top', text: 'Biota' },
			{ view: 'Slider', rect: '180 760 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 760 90 18', anchors: 'right top' },
			// Relevance for regulation
			{ view: 'Label',    rect: '15 785 100 24', anchors: 'left top', html: '<strong>Relevance for regulation</strong>' },
			{ view: 'Label', rect: '15 810 70 18', anchors: 'left top', text: 'Market_Volume' },
			{ view: 'Slider', rect: '180 810 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 810 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 835 70 18', anchors: 'left top', text: 'Wide_d_use' },
			{ view: 'Slider', rect: '180 835 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 835 90 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 860 70 18', anchors: 'left top', text: 'Political_concern' },
			{ view: 'Slider', rect: '180 860 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 860 90 18', anchors: 'right top' },
			// Need for Regulation
			{ view: 'Label',    rect: '15 885 100 24', anchors: 'left top', html: '<strong>Need for regulation</strong>' },
			{ view: 'Label', rect: '15 910 70 18', anchors: 'left top', text: 'Need for regulation' },
			{ view: 'Slider', rect: '180 910 200 18', anchors: 'left right top', values: [0, 1, 2, 3] },
			{ view: 'TextField', rect: '405 910 90 18', anchors: 'right top' },
			
			// OK
			{ view: 'Button',   rect: '10 950 200 24',  anchors: 'left top', text: 'Generate Ranking' },
	    ];
	}

	// page layout
	if (document.getElementById('wisec-ranking-form')) {
		uki(
	    	{ view: 'Box', rect: '0 0 1000 300', minSize: '980 0', anchors: 'top left right width', childViews: [
	        	{ view: 'Box', rect: '0 0 1000 100', anchors: 'top left right width', childViews: views() }
	    		]}
			).attachTo( document.getElementById('wisec-ranking-form'), '1000 300' );
	

		// Bind alert to all buttons
		uki('Button').bind('click', function() { 
		    alert("This is for demo purposes only!");
		});

		// On slider move update textfield
		uki('Slider').change(function() {
		    this.nextView().value(this.value());
		}).change();

		// Make label work as labels in browser (checkboxes)
		uki('Label').click(function() {
		   if (this.prevView().checked) this.prevView().checked(!this.prevView().checked()).focus();
		});
	
	}
	
});


