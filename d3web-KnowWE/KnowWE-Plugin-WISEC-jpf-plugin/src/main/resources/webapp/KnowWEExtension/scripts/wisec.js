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
			{ view: 'Slider', id: 'CMR', rect: '180 260 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0, value: 0 },
			{ view: 'TextField', rect: '405 260 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 285 70 18', anchors: 'left top', text: 'Persistence' },
			{ view: 'Slider', id: 'Persistence', rect: '180 285 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 285 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 310 70 18', anchors: 'left top', text: 'Bioaccumulation_Potential' },
			{ view: 'Slider', id: 'Bioaccumulation_Potential', rect: '180 310 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 310 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 335 70 18', anchors: 'left top', text: 'Aqua_Tox' },
			{ view: 'Slider', id: 'Aqua_Tox', rect: '180 335 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 335 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 360 70 18', anchors: 'left top', text: 'EDC' },
			{ view: 'Slider', id: 'EDC', rect: '180 360 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 360 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 385 70 18', anchors: 'left top', text: 'Further_Tox' },
			{ view: 'Slider', id: 'Further_Tox', rect: '180 385 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 385 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 410 70 18', anchors: 'left top', text: 'Climatic_Change' },
			{ view: 'Slider', id: 'Climatic_Change', rect: '180 410 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 410 35 18', anchors: 'right top' },
			// Mobility
			{ view: 'Label',    rect: '15 435 100 24', anchors: 'left top', html: '<strong>Mobility</strong>' },
			{ view: 'Label', rect: '15 460 70 18', anchors: 'left top', text: 'LRT' },
			{ view: 'Slider', id: 'LRT', rect: '180 460 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 460 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 485 70 18', anchors: 'left top', text: 'Water_solubility' },
			{ view: 'Slider', id: 'Water_solubility', rect: '180 485 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 485 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 510 70 18', anchors: 'left top', text: 'Adsorption' },
			{ view: 'Slider', id: 'Adsorption', rect: '180 510 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 510 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 535 70 18', anchors: 'left top', text: 'Vapor_pressure' },
			{ view: 'Slider', id: 'Vapor_pressure', rect: '180 535 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 535 35 18', anchors: 'right top' },
			// Exposure / Monitoring
			{ view: 'Label',    rect: '15 560 100 24', anchors: 'left top', html: '<strong>Exposure / Monitoring</strong>' },
			{ view: 'Label', rect: '15 585 70 18', anchors: 'left top', text: 'Air' },
			{ view: 'Slider', id: 'Air', rect: '180 585 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 585 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 610 70 18', anchors: 'left top', text: 'Soil' },
			{ view: 'Slider', id: 'Soil', rect: '180 610 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 610 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 635 70 18', anchors: 'left top', text: 'Sediment' },
			{ view: 'Slider', id: 'Sediment', rect: '180 635 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 635 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 660 70 18', anchors: 'left top', text: 'Surface_water' },
			{ view: 'Slider', id: 'Surface_water', rect: '180 660 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 660 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 685 70 18', anchors: 'left top', text: 'Sea' },
			{ view: 'Slider', id: 'Sea', rect: '180 685 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 685 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 710 70 18', anchors: 'left top', text: 'Groundwater' },
			{ view: 'Slider', id: 'Groundwater', rect: '180 710 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 710 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 735 70 18', anchors: 'left top', text: 'Drinking_water' },
			{ view: 'Slider', id: 'Drinking_water', rect: '180 735 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 735 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 760 70 18', anchors: 'left top', text: 'Biota' },
			{ view: 'Slider', id: 'Biota', rect: '180 760 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 760 35 18', anchors: 'right top' },
			// Relevance for regulation
			{ view: 'Label',    rect: '15 785 100 24', anchors: 'left top', html: '<strong>Relevance for regulation</strong>' },
			{ view: 'Label', rect: '15 810 70 18', anchors: 'left top', text: 'Market_Volume' },
			{ view: 'Slider', id: 'Market_Volume', rect: '180 810 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 810 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 835 70 18', anchors: 'left top', text: 'Wide_d_use' },
			{ view: 'Slider', id: 'Wide_d_use', rect: '180 835 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 835 35 18', anchors: 'right top' },
			{ view: 'Label', rect: '15 860 70 18', anchors: 'left top', text: 'Political_concern' },
			{ view: 'Slider', id: 'Political_concern', rect: '180 860 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 860 35 18', anchors: 'right top' },
			// Need for Regulation
			{ view: 'Label',    rect: '15 885 100 24', anchors: 'left top', html: '<strong>Need for regulation</strong>' },
			{ view: 'Label', rect: '15 910 70 18', anchors: 'left top', text: 'Need_for_regulation' },
			{ view: 'Slider', id: 'Need_for_regulation', rect: '180 910 200 18', anchors: 'left right top', values: [-3, -2, -1, 0, 1, 2, 3], value: 0 },
			{ view: 'TextField', rect: '405 910 35 18', anchors: 'right top' },
			
			// OK
			{ view: 'Button',   rect: '10 950 200 24',  anchors: 'left top', text: 'Generate Ranking' }
	    ];
	}

	
	if (document.getElementById('wisec-ranking-form')) {
		
		// page layout
		uki( { view: 'Box', rect: '0 0 1000 100', anchors: 'top left right width', childViews: views() }
	    	).attachTo( document.getElementById('wisec-ranking-form'), '1000 300' );
	
		// Bind AJAX request to button
		uki('Button').bind('click', function() {
			
			// Hide form
			document.getElementById('wisec-ranking-form').style.display = 'none';
		    
			// Get all sliders
			sliders = uki('Slider');
			
			// default params
			params = {
				action : 'WISECRankingAction',
                KWikiWeb : 'default_web',
                substances : 10,
                printinfo : 'true'
			};
			
			// add all criterias as param
			for (i = 0; i < sliders.length; i++) {
				params[sliders[i].id()] = sliders[i].value();
			}

			// options for AJAX request
            options = {
                url : KNOWWE.core.util.getURL( params ),
                response : {
                    action : 'insert',
                    ids : [ 'wisec-ranking-result' ]
                }
            }
            
            // send AJAX request
            new _KA( options ).send();
		
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


