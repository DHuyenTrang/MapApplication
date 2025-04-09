package com.example.mapapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mapapplication.databinding.FragmentLocationInforBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LocationInforFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentLocationInforBinding? = null
    private val binding get() = _binding!!

//    companion object {
//        private const val ARG_LATITUDE = "lat"
//        private const val ARG_LONGITUDE = "lon"
//
//        fun newInstance(lat: Double, lon: Double): LocationInforFragment {
//            val fragment = LocationInforFragment()
//            val args = Bundle()
//            args.putDouble(ARG_LATITUDE, lat)
//            args.putDouble(ARG_LONGITUDE, lon)
//            fragment.arguments = args
//            return fragment
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLocationInforBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}